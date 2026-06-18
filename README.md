# baro-server

Kotlin Spring Boot 기반 MSA 프로젝트입니다. 현재는 멀티모듈 모노리포에서 서비스별 Spring Boot 애플리케이션을 분리하고, Gateway를 외부 진입점으로 두는 구조입니다.

## 구성

- `control-service`: 차량 상태/위치 조회를 담당하는 관제 서비스
- `dispatch-service`: 배차 조회를 담당하는 배차 서비스
- `gateway-service`: 외부 요청을 각 서비스로 라우팅하는 API Gateway
- `relocation-service`: 차량 재배치 흐름을 담당하는 재배치 서비스
- `user-service`: 사용자 인증과 사용자 정보를 담당하는 사용자 서비스

각 서비스는 독립적인 Spring Boot 애플리케이션으로 구성되어 있으며 루트에서는 멀티모듈 Gradle 프로젝트로 관리합니다.

## 디렉터리 구조

```text
baro-server
├── build.gradle.kts
├── gradlew
├── settings.gradle.kts
├── common-core
├── common-kakao
├── common-web
├── control-service
├── dispatch-service
├── gateway-service
├── relocation-service
└── user-service
```

## MSA 구조 원칙

- 서비스 간 Gradle 직접 의존은 두지 않습니다.
- 각 서비스는 자기 도메인과 데이터를 소유합니다.
- 공통 모듈에는 서비스 독립적인 인프라/계약만 둡니다.
- 서비스별 비즈니스 로직, 도메인 모델, 유스케이스 DTO는 common 모듈로 올리지 않습니다.
- 외부 진입 요청은 `gateway-service`를 우선 통과시킵니다.
- 서비스 간 내부 통신은 명시적인 internal API, service-to-service 인증, 이벤트/메시징 계약을 통해 관리합니다.
- in-memory 상태는 수평 확장이 필요한 흐름에서는 사용하지 않고 Redis/DB/Kafka 같은 공유 저장소로 옮기는 것을 우선 검토합니다.

## 공통 모듈

- `common-core`: Spring 의존성을 최소화한 공통 예외 베이스와 범용 모델
- `common-web`: 공통 응답 포맷, REST 예외 응답, Jackson, Clock, OpenAPI 설정
- `common-kakao`: 여러 서비스에서 함께 쓰는 카카오모빌리티 API 클라이언트와 외부 응답 모델

서비스 모듈은 필요한 공통 모듈만 의존합니다. 도메인 로직이나 특정 서비스 유스케이스 변환 로직은 공통 모듈로 옮기지 않고 각 서비스 안에 둡니다.

### common-core 사용 기준

MSA에서 공통 모듈은 서비스 독립성을 해치지 않는 작은 범위로만 사용합니다. `common-core`는 현재 다음처럼 서비스 내부와 공통 인프라에서 함께 쓰는 최소 예외 계약만 담당합니다.

- `BaroException`
- `BadRequestException`
- `ExternalServiceException`

이 예외들은 `common-web`의 REST 예외 응답 변환, `common-kakao`의 외부 API 실패 표현, 각 서비스의 도메인/애플리케이션 예외 베이스로 사용합니다.

Gateway는 라우팅, 외부 진입점, 1차 차단 정책을 담당하므로 `common-core`의 예외 베이스를 대체하지 않습니다. Gateway로 옮길 수 있는 기능이 생기더라도 다음 기준을 지킵니다.

- Gateway로 이동 가능: 라우팅, CORS, rate limit, 공통 헤더, trace id, 외부 진입 인증/인가의 1차 처리
- Gateway로 이동 금지: 도메인 예외, 서비스별 비즈니스 판단, 서비스별 DTO 변환, 내부 유스케이스 로직

따라서 현재 `common-core` 기능은 Gateway로 옮기지 않고 작게 유지합니다.

### 인증 처리 기준

외부 요청의 JWT 인증은 `gateway-service`에서 처리합니다. Gateway는 유효한 JWT를 검증한 뒤 downstream 서비스에 다음 헤더를 주입합니다.

```text
X-Authenticated-User-Id
X-Authenticated-Email
```

업무 서비스는 JWT를 직접 검증하지 않고 Gateway가 주입한 인증 헤더를 사용합니다. 토큰 발급은 `user-service` 책임으로 유지합니다.

필요한 환경변수는 다음과 같습니다.

```text
JWT_SECRET=32바이트_이상의_비밀키
```

## Gateway

`gateway-service`는 Spring Cloud Gateway 기반의 외부 진입점입니다.

- 기본 포트: `8080`
- Spring Cloud BOM: `2023.0.3`
- Spring Boot `3.3.5`와의 호환성을 위해 Spring Cloud `2023.0.3`을 사용합니다.
- Gateway는 WebFlux 기반이므로 Servlet/MVC 기반의 `common-web`을 직접 의존하지 않습니다.
- 현재 Gateway는 라우팅, 외부 JWT 인증, 내부 API 차단을 담당하며, 비즈니스 로직은 각 서비스에 둡니다.
- dev 인프라에서는 `baro-terraform`의 ALB가 업무 API 경로를 `gateway-service`로 전달하고, Gateway가 내부 DNS로 각 서비스에 전달합니다.

기본 라우팅은 다음과 같습니다.

| Gateway 경로 | 대상 서비스 |
| --- | --- |
| `/user/**` | `user-service` |
| `/dispatch/**` | `dispatch-service` |
| `/control/**` | `control-service` |
| `/relocation/assign` | `relocation-service` |

인증 제외 경로는 다음과 같습니다.

- `/user/auth/sign-up`
- `/user/auth/login`
- `/user/auth/token/refresh`

그 외 업무 API 경로는 Gateway에서 JWT 검증 후 내부 서비스로 전달합니다. Gateway는 클라이언트가 보낸 `X-Authenticated-*` 헤더를 제거하고 검증된 JWT claim 기반으로 다시 주입합니다.

다음 내부 API는 Gateway에서 기본 차단합니다.

- `/internal/**`
- `/dispatch/command-ack`
- `/dispatch/arrived`
- `/dispatch/vehicles/*/active`

로컬 실행 시 대상 서비스 URL은 환경변수로 바꿀 수 있습니다.

```text
GATEWAY_PORT=8080
JWT_SECRET=32바이트_이상의_비밀키
USER_SERVICE_URL=http://localhost:8084
DISPATCH_SERVICE_URL=http://localhost:8082
CONTROL_SERVICE_URL=http://localhost:8081
RELOCATION_SERVICE_URL=http://localhost:8083
```

dev ECS 배포에서는 Terraform이 다음 형태의 Cloud Map 내부 DNS를 Gateway 환경변수로 주입합니다.

```text
USER_SERVICE_URL=http://user-service.baro.internal:8084
DISPATCH_SERVICE_URL=http://dispatch-service.baro.internal:8082
CONTROL_SERVICE_URL=http://control-service.baro.internal:8081
RELOCATION_SERVICE_URL=http://relocation-service.baro.internal:8083
```

`/internal/**` API를 제공하는 서비스는 Secret Manager의 `INTERNAL_API_KEY`를 환경변수로 주입받고, 내부 호출자는 다음 헤더를 함께 전달해야 합니다.

```http
X-Internal-Api-Key: {INTERNAL_API_KEY}
```

## 개발 환경

- Java 21
- Gradle Wrapper 사용

## 빌드 및 실행

루트 멀티모듈 프로젝트이므로, 루트에서 전체 또는 모듈별 태스크를 실행합니다.

### 전체 빌드

```bash
./gradlew build
```

### 모듈별 빌드

```bash
./gradlew :control-service:build
./gradlew :dispatch-service:build
./gradlew :gateway-service:build
./gradlew :relocation-service:build
./gradlew :user-service:build
```

### 모듈별 실행

```bash
./gradlew :control-service:bootRun
./gradlew :dispatch-service:bootRun
./gradlew :gateway-service:bootRun
./gradlew :relocation-service:bootRun
./gradlew :user-service:bootRun
```

### 카카오 API 환경변수 설정

`dispatch-service`, `relocation-service`는 `common-kakao`를 통해 카카오모빌리티 API를 사용합니다.
카카오 설정은 `common-kakao`의 `KakaoMobilityProperties`에서 관리하며, 각 서비스의 `application.yml`에는 중복 선언하지 않습니다.

로컬 실행 시 IntelliJ Run Configuration의 `Environment variables`에 다음 값을 추가합니다.

```text
KAKAO_MOBILITY_API_KEY=your_kakao_rest_api_key
```

`KAKAO_MOBILITY_BASE_URL`은 기본값이 있어 보통 설정하지 않아도 됩니다.

GitHub Actions에서 실제 카카오 API를 호출하는 테스트를 실행할 경우, GitHub Secrets에 같은 이름으로 등록합니다.

```text
KAKAO_MOBILITY_API_KEY
```

CLI로 실행해야 할 때는 현재 터미널 세션에 환경변수를 설정한 뒤 실행합니다.

```bash
$env:KAKAO_MOBILITY_API_KEY="your_kakao_rest_api_key"
.\gradlew.bat :dispatch-service:bootRun
```

## IntelliJ 실행

- 프로젝트는 루트 `build.gradle.kts` 기준으로 엽니다.
- `Gradle JVM`, `Project SDK`, Run Configuration JRE는 모두 Java 21로 맞춥니다.
- 각 서비스는 해당 `Application` 클래스를 기준으로 실행합니다.
  - `control-service`: `com.baro.control.ControlServiceApplicationKt`
  - `dispatch-service`: `com.baro.dispatch.DispatchServiceApplicationKt`
  - `gateway-service`: `com.baro.gateway.GatewayServiceApplicationKt`
  - `relocation-service`: `com.baro.relocation.RelocationServiceApplicationKt`
  - `user-service`: `com.baro.user.UserServiceApplicationKt`

## 기본 포트

- `gateway-service`: `8080`
- `control-service`: `8081`
- `dispatch-service`: `8082`
- `relocation-service`: `8083`
- `user-service`: `8084`

## Swagger 문서

업무 서비스는 `common-web`의 공통 OpenAPI 설정을 사용합니다. Gateway는 현재 OpenAPI 문서를 집계하지 않습니다.

- `control-service`: `http://localhost:8081/swagger-ui.html`
- `dispatch-service`: `http://localhost:8082/swagger-ui.html`
- `relocation-service`: `http://localhost:8083/swagger-ui.html`
- `user-service`: `http://localhost:8084/swagger-ui.html`

OpenAPI JSON 문서는 각 서비스의 `/api-docs`에서 확인합니다.

## CI

GitHub Actions는 `paths` 기반 변경 감지로 필요한 모듈의 빌드/테스트를 수행합니다. 현재 workflow에는 dev ECS 배포 job도 포함되어 있습니다.
변경 대상이 아닌 서비스 workflow는 실행하지 않아 PR checks에 표시하지 않습니다.

- 루트 Gradle 설정, Gradle Wrapper 변경: 5개 서비스 모두 빌드
- 서비스별 CI workflow 변경: 해당 서비스 빌드
- `common-core`, `common-web` 변경: 업무 서비스 모두 빌드
- `common-kakao` 변경: `dispatch-service`, `relocation-service` 빌드
- `gateway-service` 변경: `gateway-service` 빌드
- `control-service`, `dispatch-service`, `relocation-service`, `user-service` 변경: 해당 서비스만 빌드

각 서비스 빌드는 다음 형태로 실행됩니다.

```bash
./gradlew :control-service:clean :control-service:build
./gradlew :dispatch-service:clean :dispatch-service:build
./gradlew :gateway-service:clean :gateway-service:build
./gradlew :relocation-service:clean :relocation-service:build
./gradlew :user-service:clean :user-service:build
```

dev ECS 배포 대상에는 `gateway-service`도 포함됩니다. 단, Gateway 배포는 `baro-terraform`에 gateway ECS 서비스/ECR/ALB 라우팅이 먼저 반영되어 있어야 합니다.

## 로컬 실행

- 포트 사용 확인: lsof -i :{포트번호}
- 포트 죽이기 kill -9 {PID}

### 로컬 API 테스트 계정

회원가입 → 로그인 → PRE배차 흐름을 확인할 때는 아래 테스트 계정을 사용할 수 있습니다.

```text
email=baro-local-test@example.com
password=baro2026!
```

PRE배차 요청은 Gateway 주소로 보내고 로그인 응답의 `access_token`을 `Authorization: Bearer {access_token}` 헤더에 넣습니다.
사용자 ID는 Gateway가 검증한 뒤 `X-Authenticated-User-Id` 헤더로 dispatch-service에 전달하므로 요청 본문에는 넣지 않습니다.

1. user-service+ user db
```
docker compose -f docker-compose.user-service.yml down
docker compose -f docker-compose.user-service.yml up -d
./gradlew :user-service:bootRun --args='--spring.profiles.active=local'
```

## 테스트 원칙

공통 모듈은 테스트가 없어야 하는 구조가 아닙니다. 단순 DTO, 예외 타입, 설정 골격만 있을 때는 테스트를 생략할 수 있지만, 여러 서비스의 계약에 영향을 주는 코드는 공통 모듈에서 직접 검증합니다.

- `common-web`: `BaseResponse`, 에러 응답 포맷, Jackson snake_case, OpenAPI 기본 설정
- `common-kakao`: 실제 카카오 API 호출 없이 Mock HTTP로 요청 경로, 헤더, 쿼리 파라미터, 에러 매핑 검증
- `common-core`: 동작이 없는 예외 타입만 있다면 별도 테스트보다 사용하는 모듈의 테스트로 검증
