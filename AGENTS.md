# AGENTS.md

## 작업 범위

- dispatch-service는 DDD 구조로 작업한다.
- control-service 등 다른 서비스는 명시 요청이 없는 한 수정하지 않는다.
- 공통화는 실제로 공유되는 코드, 서비스 독립적인 기술 설정, 서비스 간 계약으로 합의된 모델만 대상으로 한다.
- 특정 서비스 비즈니스 로직, 특정 외부 연동 전용 코드, 미래를 위한 추상화는 공통 모듈로 옮기지 않는다.
- 배포(CD)는 아직 추가하지 않는다.

## 공통 모듈 구조

- `common-core`: Spring 의존성을 최소화한 공통 예외 베이스와 범용 모델을 둔다.
- `common-kakao`: 여러 서비스가 쓰는 카카오 API HTTP 클라이언트, 설정, 외부 응답 DTO를 둔다.
- `common-web`: Spring MVC/Jackson/OpenAPI/REST 예외 응답 같은 웹 계층 공통 설정을 둔다.
- 서비스 모듈은 필요한 공통 모듈만 의존한다.
- common 모듈이 비대해지지 않도록 도메인 공통화는 중복이 2회 이상 생긴 뒤 검토한다.
- 외부 API 공통 모듈은 외부 API 호출과 원본 응답 모델까지만 제공하고, 서비스 유스케이스 포트 변환은 각 서비스에 둔다.

## dispatch-service 패키지 구조

- `domain`: 도메인 모델, 도메인 예외, repository 계약을 둔다.
- `application`: 유스케이스 서비스와 외부 연동 port를 둔다.
- `infrastructure`: DB, 외부 API, 설정 등 기술 구현체를 둔다.
- `interfaces.rest`: REST controller, REST DTO, REST 예외 핸들러를 둔다.
- REST 예외 응답, Jackson snake_case, Clock, OpenAPI 기본 설정은 `common-web`을 우선 사용한다.

## PRE배차 API

- 엔드포인트: `POST /dispatch/pre`
- REST API 경로는 `DispatchApiPaths`에 상수화한다.
- 카카오모빌리티 API 경로는 `KakaoMobilityApiPaths`에 상수화한다.
- PRE배차 요청은 사용자 ID, 출발지, 목적지를 받는다.
- 카카오모빌리티 자동차 길찾기 API로 예상 요금, 경로, 소요시간, 거리를 조회한다.
- `dispatch_request` 테이블에 `pending` 상태의 배차 요청을 생성한다.
- 좌표는 `POINT(lon lat)` 형태로 저장한다.

## 설정 관리

- `application.yml`은 깃에 올린다.
- 실제 비밀값은 환경변수로 관리한다.
- dispatch-service 주요 환경변수:
  - `DISPATCH_DB_URL`
  - `DISPATCH_DB_USERNAME`
  - `DISPATCH_DB_PASSWORD`
  - `KAKAO_MOBILITY_BASE_URL`
  - `KAKAO_MOBILITY_API_KEY`

## 테스트와 메시지

- 테스트 함수명은 한국어로 작성한다.
- 사용자에게 노출될 수 있는 에러 메시지는 한국어로 작성한다.
- JSON 필드명, DB 컬럼명, 외부 API 파라미터명처럼 프로토콜에 속한 문자열은 기존 표기를 유지한다.
- common 모듈은 테스트가 없는 것을 정석으로 보지 않는다.
- 단순 예외 타입, DTO, 설정 골격만 있을 때는 테스트를 생략할 수 있다.
- 공통 응답 포맷, Jackson 설정, 예외 응답, 외부 API 클라이언트처럼 여러 서비스의 계약에 영향을 주는 코드는 우선적으로 테스트한다.
- `common-kakao` 테스트는 실제 카카오 API를 호출하지 않고 Mock HTTP 방식으로 요청 경로, 헤더, 쿼리 파라미터, 에러 매핑을 검증한다.

## CI

- GitHub Actions CI는 정상 빌드와 테스트 통과만 확인한다.
- 하나의 workflow 안에서 변경 감지 기반 모듈별 빌드를 수행한다.
- 루트 Gradle 설정, Gradle Wrapper, CI workflow가 바뀌면 3개 서비스 빌드를 모두 수행한다.
- `common-core`, `common-web`이 바뀌면 3개 서비스 빌드를 모두 수행한다.
- `common-kakao`가 바뀌면 `dispatch-service`, `redispatch-service` 빌드를 수행한다.
- 서비스 모듈만 바뀌면 해당 서비스 빌드만 수행한다.
- 각 서비스 빌드는 `./gradlew :{service}:clean :{service}:build` 형태로 실행한다.
- CD 단계는 추가하지 않는다.

## 커밋 메시지

- 형식: `{작업유형}: {작업내용}`
- 작업유형은 영어로 작성한다.
- 작업내용은 한국어로 간략하게 작성한다.
- 예: `feat: PRE배차 DTO 추가`
