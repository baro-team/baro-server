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

### 카카오 API 사용 서비스 실행 예시

`dispatch-service`, `redispatch-service`는 `common-kakao`를 통해 카카오모빌리티 API를 사용합니다.
로컬에서는 Spring profile `local`을 사용하거나 환경변수 `KAKAO_MOBILITY_API_KEY`를 설정합니다.

1. 예시 파일을 복사

```bash
cp dispatch-service/src/main/resources/application-local.yml.example \
   dispatch-service/src/main/resources/application-local.yml
```

2. `application-local.yml` 또는 환경변수에 카카오 REST API 키 입력

```yaml
kakao:
  mobility:
    api-key: your_kakao_rest_api_key
```

3. 실행

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew :dispatch-service:bootRun
```

```bash
KAKAO_MOBILITY_API_KEY=your_kakao_rest_api_key ./gradlew :redispatch-service:bootRun
```

배포 환경에서는 `application-local.yml`을 사용하지 않고 환경변수 `KAKAO_MOBILITY_API_KEY`를 사용합니다.

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
