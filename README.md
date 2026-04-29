# baro-server

Kotlin Spring Boot 기반 MSA 최소 프로젝트 세팅입니다.

## 구성

- `control-service`: 차량 상태/위치 조회를 담당하는 관제 서비스
- `dispatch-service`: 배차 조회를 담당하는 배차 서비스
- `redispatch-service`: 재배차 흐름을 담당할 재배차 서비스

두 서비스는 독립적인 Spring Boot 애플리케이션으로 구성되어 있으며 루트에서는 멀티모듈 Gradle 프로젝트로 관리합니다.

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
└── redispatch-service
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
./gradlew :redispatch-service:build
```

### 모듈별 실행

```bash
./gradlew :control-service:bootRun
./gradlew :dispatch-service:bootRun
./gradlew :redispatch-service:bootRun
```

### 카카오 API 환경변수 설정

`dispatch-service`, `redispatch-service`는 `common-kakao`를 통해 카카오모빌리티 API를 사용합니다.
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
  - `redispatch-service`: `com.baro.redispatch.RedispatchServiceApplicationKt`

## 기본 포트

- `control-service`: `8081`
- `dispatch-service`: `8082`
- `redispatch-service`: `8083`

## Swagger 문서

세 서비스는 `common-web`의 공통 OpenAPI 설정을 사용합니다.

- `control-service`: `http://localhost:8081/swagger-ui.html`
- `dispatch-service`: `http://localhost:8082/swagger-ui.html`
- `redispatch-service`: `http://localhost:8083/swagger-ui.html`

OpenAPI JSON 문서는 각 서비스의 `/api-docs`에서 확인합니다.
