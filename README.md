# baro-server

Kotlin Spring Boot 기반 MSA 최소 프로젝트 세팅입니다.

## 구성

- `control-service`: 차량 상태/위치 조회를 담당하는 관제 서비스
- `dispatch-service`: 배차 조회를 담당하는 배차 서비스

두 서비스는 독립적인 Spring Boot 애플리케이션으로 구성되어 있으며 루트에서는 멀티모듈 Gradle 프로젝트로 관리합니다.

## 디렉터리 구조

```text
baro-server
├── build.gradle.kts
├── settings.gradle.kts
├── control-service
└── dispatch-service
```

## 실행 예시

Gradle Wrapper는 아직 포함하지 않았으므로, 로컬 Gradle 또는 IDE 실행 설정으로 각 서비스를 실행하면 됩니다.

```bash
gradle :control-service:bootRun
gradle :dispatch-service:bootRun
```

## 기본 포트

- `control-service`: `8081`
- `dispatch-service`: `8082`
