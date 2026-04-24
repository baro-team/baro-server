# control-service

바로(BARO) 시스템의 차량 제어 허브 서비스입니다. MQTT를 통해 차량에서 올라오는 실시간 텔레메트리를 수신하고 Redis에 상태를 저장하며, REST API를 통해 외부 서비스가 차량에 명령을 내릴 수 있게 합니다.

---

## 시스템에서의 역할

```
차량 (baro-edge)
    │  MQTT pub  vehicles/{id}/telemetry|events|snapshot
    ▼
[Mosquitto / AWS IoT Core]
    │
    ▼
control-service  ←─── REST API ─── navigation HTML / dispatch-service
    │
    ├─ Redis (차량 최신 상태 저장)
    └─ Redis Pub/Sub (vehicle-location:{id} → 다른 서비스 실시간 전달)
```

- **inbound**: 차량 MQTT 메시지 수신 → Redis 상태 갱신
- **outbound**: REST API 수신 → 차량에 MQTT 명령 전송
- **상태 조회**: REST API로 전체/개별 차량 상태 반환

---

## 기술 스택

| 항목 | 선택 |
|---|---|
| 언어 | Kotlin 1.9.25 |
| 프레임워크 | Spring Boot 3.3.5 |
| MQTT 클라이언트 | Spring Integration MQTT (Eclipse Paho) |
| 메시지 라우팅 | Spring Integration (DirectChannel + @ServiceActivator) |
| 상태 저장 | Redis 3.2 (GEO, Hash, Set) |
| TLS (AWS 모드) | BouncyCastle `bcpkix-jdk18on:1.78.1` |
| HTTP 클라이언트 | Spring `RestClient` (Boot 3.2+) |
| 포트 | 8081 |

---

## 패키지 구조

```
src/main/kotlin/com/baro/control/
├── ControlServiceApplication.kt     # 진입점
├── config/
│   ├── MqttProperties.kt            # YAML → Kotlin 바인딩
│   ├── MqttConfig.kt                # Spring Integration MQTT 빈 설정
│   ├── SslUtil.kt                   # AWS IoT Core TLS 소켓팩토리
│   └── WebConfig.kt                 # CORS 설정
├── mqtt/
│   ├── MqttSubscriber.kt            # 수신 메시지 라우터
│   └── MqttPublisher.kt             # 명령 발행
├── service/
│   ├── TelemetryService.kt          # 위치·상태 처리
│   └── EventService.kt              # 이벤트(도착 등) 처리
├── redis/
│   ├── VehicleRedisRepository.kt    # GEO/Hash/Set CRUD
│   └── RedisPublisher.kt            # Pub/Sub 발행
├── client/
│   └── DispatchServiceClient.kt     # dispatch-service HTTP 호출
├── controller/
│   └── VehicleController.kt         # REST API
└── dto/
    ├── TelemetryPayload.kt
    ├── SnapshotPayload.kt
    ├── EventPayload.kt
    ├── AckPayload.kt
    ├── BufferedPayload.kt
    ├── VehicleStatus.kt
    └── CommandRequest.kt
```

---

## 레이어별 설명

### 1. MQTT 연결 설정 (`config/MqttConfig.kt`)

Spring Integration의 `MqttPahoClientFactory`를 환경변수 `MQTT_MODE`에 따라 두 가지로 초기화합니다.

| 모드 | 연결 | 인증 |
|---|---|---|
| `local` | `tcp://{host}:{port}` | 없음 |
| `aws` | `ssl://{endpoint}:8883` | mTLS (X.509 인증서) |

빈 4개를 등록합니다:

```
mqttClientFactory  ─ Paho 연결 설정 (URL, TLS, reconnect)
mqttInputChannel   ─ DirectChannel (수신 메시지 통로)
mqttInboundAdapter ─ 5개 토픽 구독 → mqttInputChannel로 전달
mqttOutboundChannel─ DirectChannel (발신 메시지 통로)
mqttOutboundHandler─ @ServiceActivator → Paho로 실제 발행
```

`DirectChannel`은 발신자와 수신자를 동기적으로 연결하는 Spring Integration의 기본 채널입니다.

### 2. AWS IoT Core TLS (`config/SslUtil.kt`)

AWS IoT Core는 PKCS#1 또는 PKCS#8 형식의 RSA 개인키를 사용합니다. BouncyCastle을 이용해 두 형식 모두 파싱한 뒤 `SSLContext`를 생성합니다.

```
CA 인증서 (AmazonRootCA1.pem)
    + 클라이언트 인증서 (device.pem.crt)
    + 개인키 (private.pem.key)
    → SSLSocketFactory
```

### 3. 설정 바인딩 (`config/MqttProperties.kt`)

`@ConfigurationProperties(prefix = "mqtt")`로 `application.yml`의 MQTT 섹션을 Kotlin data class에 자동 바인딩합니다.

### 4. 메시지 수신 (`mqtt/MqttSubscriber.kt`)

`@ServiceActivator(inputChannel = "mqttInputChannel")`으로 모든 수신 메시지를 받아 토픽 경로를 파싱합니다.

```
vehicles/{vehicleId}/telemetry          → TelemetryService.handleTelemetry()
vehicles/{vehicleId}/telemetry/buffered → 배열 분해 후 handleTelemetry() 반복
vehicles/{vehicleId}/events             → EventService.handleEvent()
vehicles/{vehicleId}/snapshot           → TelemetryService.handleSnapshot()
vehicles/{vehicleId}/ack                → 로그만 출력
```

### 5. 텔레메트리 처리 (`service/TelemetryService.kt`)

```
handleTelemetry(vehicleId, payload)
  ├─ repo.updateLocation()  → Redis GEO에 위도·경도 저장
  ├─ repo.updateInfo()      → Redis Hash에 속도·방향·배터리 등 저장
  └─ publisher.publish()    → Redis Pub/Sub "vehicle-location:{id}"

handleSnapshot(vehicleId, payload)
  └─ repo.updateInfo()      → Hash에 엔진오일·브레이크오일·냉각수 등 저장
```

### 6. Redis 데이터 모델 (`redis/VehicleRedisRepository.kt`)

Redis에는 차량당 **가장 최신 상태만** 유지됩니다 (덮어쓰기).

| 키 패턴 | 자료구조 | 저장 내용 |
|---|---|---|
| `vehicle:locations` | GEO Set | 전체 차량 위도·경도 |
| `car:{id}:info` | Hash | 속도, 방향, 배터리, 상태, tripId, 마지막수신시각 |
| `vehicle:ids` | Set | 등록된 vehicleId 목록 |

> GEO는 Redis 3.2에서 도입되었습니다. Redis 3.0에서는 동작하지 않습니다.
> `redis.opsForGeo().add(key, Point(lng, lat), memberId)` — Point는 **(경도, 위도)** 순서입니다.

### 7. 이벤트 처리 (`service/EventService.kt`)

```
ARRIVED → DispatchServiceClient.notifyArrived(vehicleId, tripId)
WARNING  → 로그 출력
```

### 8. 명령 발행 (`mqtt/MqttPublisher.kt`)

REST API로 받은 `CommandRequest`를 JSON으로 직렬화한 뒤 `vehicles/{id}/commands` 토픽으로 QoS 1 발행합니다.

```kotlin
mqttOutboundChannel ← Message(json, headers[TOPIC, QOS])
                          ↓
                  MqttPahoMessageHandler (Paho)
                          ↓
              vehicles/{vehicleId}/commands
```

### 9. REST API (`controller/VehicleController.kt`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/vehicles` | 전체 차량 상태 목록 |
| GET | `/vehicles/{id}` | 특정 차량 상태 |
| POST | `/vehicles/{id}/command` | 차량에 명령 전송 |

**CommandRequest 바디 예시:**
```json
{
  "type": "DISPATCH",
  "trip_id": "trip-001",
  "route": [{"lat": 37.5, "lng": 127.0}, {"lat": 37.6, "lng": 127.1}]
}
```

**VehicleStatus 응답 예시:**
```json
{
  "vehicleId": "1001",
  "latitude": 37.5012,
  "longitude": 127.0396,
  "speed": 30,
  "heading": 270.5,
  "battery": 88.3,
  "autonomyMode": "AUTO",
  "status": "DRIVING",
  "tripId": "trip-001",
  "lastSeen": "2024-01-01T12:00:00Z"
}
```

---

## 환경변수 참조

| 변수 | 기본값 | 설명 |
|---|---|---|
| `MQTT_MODE` | `local` | `local` 또는 `aws` |
| `LOCAL_MQTT_HOST` | `localhost` | Mosquitto 호스트 |
| `LOCAL_MQTT_PORT` | `1883` | Mosquitto 포트 |
| `IOT_ENDPOINT` | — | AWS IoT Core 엔드포인트 |
| `IOT_CERT_PATH` | — | 클라이언트 인증서 경로 (.pem.crt) |
| `IOT_KEY_PATH` | — | 개인키 경로 (.pem.key) |
| `IOT_CA_PATH` | — | CA 인증서 경로 (AmazonRootCA1.pem) |
| `REDIS_HOST` | `localhost` | Redis 호스트 |
| `REDIS_PORT` | `6379` | Redis 포트 |
| `DISPATCH_SERVICE_URL` | `http://localhost:8082` | dispatch-service 주소 |

---

## 실행 방법

### 사전 조건

- Java 21
- Redis 3.2 이상 실행 중
- Mosquitto (local 모드) 또는 AWS IoT Core 인증서 (aws 모드)

### 로컬 모드 실행

```bash
# 프로젝트 루트에서
./gradlew :control-service:bootRun
```

### AWS 모드 실행

```bash
MQTT_MODE=aws \
IOT_ENDPOINT=xxxxxxxxxxxx-ats.iot.ap-northeast-2.amazonaws.com \
IOT_CERT_PATH=/certs/device.pem.crt \
IOT_KEY_PATH=/certs/private.pem.key \
IOT_CA_PATH=/certs/AmazonRootCA1.pem \
./gradlew :control-service:bootRun
```

---

## 구독하는 MQTT 토픽

| 토픽 | QoS | 설명 |
|---|---|---|
| `vehicles/+/telemetry` | 1 | 실시간 위치·속도·배터리 |
| `vehicles/+/telemetry/buffered` | 1 | 오프라인 중 쌓인 텔레메트리 배열 |
| `vehicles/+/events` | 1 | 도착·경고 등 이벤트 |
| `vehicles/+/snapshot` | 1 | 엔진오일·브레이크오일 등 정기 점검 데이터 |
| `vehicles/+/ack` | 1 | 명령 수신 확인 |

## 발행하는 MQTT 토픽

| 토픽 | QoS | 설명 |
|---|---|---|
| `vehicles/{id}/commands` | 1 | 차량 제어 명령 (배차·복귀 등) |

---

## 의존 서비스

| 서비스 | 역할 |
|---|---|
| Redis 3.2+ | 차량 상태 저장 및 Pub/Sub |
| Mosquitto (local) / AWS IoT Core (aws) | MQTT 브로커 |
| dispatch-service | 도착 이벤트 수신 (`POST /dispatch/arrived`) |
