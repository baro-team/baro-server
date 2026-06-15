# AGENTS.md

## 작업 범위

- dispatch-service는 DDD 구조로 작업한다.
- gateway-service는 외부 진입점과 라우팅만 담당한다. 비즈니스 로직, 서비스별 DTO 변환, 도메인 판단은 넣지 않는다.
- control-service 등 다른 서비스는 명시 요청이 없는 한 수정하지 않는다.
- 한 파일은 가능한 하나의 주요 클래스만 담당하게 한다. 단, 해당 파일 안에서만 쓰이는 하위 타입, 작은 enum, 밀접한 예외 묶음처럼 분리 이득이 낮은 경우는 함께 둘 수 있다.
- 공통화는 실제로 공유되는 코드, 서비스 독립적인 기술 설정, 서비스 간 계약으로 합의된 모델만 대상으로 한다.
- 특정 서비스 비즈니스 로직, 특정 외부 연동 전용 코드, 미래를 위한 추상화는 공통 모듈로 옮기지 않는다.
- 배포(CD)는 기존 workflow 범위를 넘겨 임의로 추가하지 않는다. 새 서비스 배포는 인프라/ECS 리소스가 합의된 뒤 별도 작업으로 진행한다.

## 공통 모듈 구조

- `common-core`: Spring 의존성을 최소화한 공통 예외 베이스와 범용 모델을 둔다.
- `common-kakao`: 여러 서비스가 쓰는 카카오 API HTTP 클라이언트, 설정, 외부 응답 DTO를 둔다.
- `common-web`: Spring MVC/Jackson/OpenAPI/REST 예외 응답 같은 웹 계층 공통 설정을 둔다.
- 서비스 모듈은 필요한 공통 모듈만 의존한다.
- common 모듈이 비대해지지 않도록 도메인 공통화는 중복이 2회 이상 생긴 뒤 검토한다.
- 외부 API 공통 모듈은 외부 API 호출과 원본 응답 모델까지만 제공하고, 서비스 유스케이스 포트 변환은 각 서비스에 둔다.
- `gateway-service`는 WebFlux 기반 Spring Cloud Gateway이므로 Servlet/MVC 기반 `common-web`을 직접 의존하지 않는다.
- 외부 요청 JWT 인증은 `gateway-service`에서 처리한다. Gateway는 검증된 사용자 정보를 `X-Authenticated-User-Id`, `X-Authenticated-Email` 헤더로 downstream 서비스에 전달한다.
- `common-web`은 validation 예외, 공통 REST 예외 응답, Jackson/OpenAPI/Clock처럼 MVC 서비스 공통 기술 설정을 흡수한다. 서비스별 도메인 예외 의미나 DTO 변환은 넣지 않는다.

## MSA 경계 원칙

- 서비스 간 Gradle 직접 의존을 만들지 않는다.
- 서비스 간 통신은 REST internal API, Kafka 이벤트, 외부 API port처럼 명시적인 계약으로만 연결한다.
- 서비스별 도메인 모델, JPA Entity, 유스케이스 DTO를 common 모듈로 올리지 않는다.
- 공통화보다 서비스 자율성을 우선한다. 단, 보안/웹 응답/외부 API raw client처럼 서비스 독립적인 기술 계약은 common 모듈을 사용한다.
- `common-core`는 shared-kernel처럼 아주 작고 안정적인 공통 계약만 둔다. 현재는 공통 예외 베이스만 유지한다.
- API Gateway는 `common-core`를 대체하지 않는다. Gateway에는 라우팅, 외부 진입 보안, CORS, rate limit, 공통 헤더 같은 edge 책임만 둔다.
- 수평 확장이 필요한 상태는 JVM in-memory store에 오래 두지 않는다. Redis, DB, Kafka, outbox/inbox 같은 durable/shared 저장소를 우선 검토한다.
- 내부 callback/API는 외부 Gateway에 노출하지 않는다. 필요한 경우 service-to-service 인증, network policy, mTLS, gateway internal route 중 하나를 적용한다.

## gateway-service 구조

- 기본 포트는 `8080`이다.
- `/user/**`는 `user-service`로 라우팅한다.
- `/dispatch/**`는 `dispatch-service`로 라우팅한다.
- `/control/**`는 `control-service`로 라우팅한다.
- `/relocation/assign`은 `relocation-service`로 라우팅한다.
- `/user/auth/sign-up`, `/user/auth/login`, `/user/auth/token/refresh`는 인증 없이 `user-service`로 라우팅한다.
- 그 외 업무 API는 Gateway에서 JWT를 검증한 뒤 `X-Authenticated-User-Id`, `X-Authenticated-Email` 헤더를 주입한다.
- `/internal/**`, `/dispatch/command-ack`, `/dispatch/arrived`, `/dispatch/vehicles/*/active`는 Gateway에서 기본 차단한다.
- 대상 서비스 URL은 `USER_SERVICE_URL`, `DISPATCH_SERVICE_URL`, `CONTROL_SERVICE_URL`, `RELOCATION_SERVICE_URL` 환경변수로 관리한다.
- dev 인프라에서는 ALB가 업무 API 경로를 `gateway-service`로 전달하고, Gateway는 Cloud Map 내부 DNS로 각 서비스에 전달한다.

## dispatch-service 패키지 구조

- `domain`: 도메인 모델, 도메인 예외, repository 계약을 둔다.
- `application`: 유스케이스 서비스와 외부 연동 port를 둔다.
- `infrastructure`: DB, 외부 API, 설정 등 기술 구현체를 둔다.
- `interfaces.rest`: REST controller, REST DTO, REST 예외 핸들러를 둔다.
- REST 예외 응답, Jackson snake_case, Clock, OpenAPI 기본 설정은 `common-web`을 우선 사용한다.

## Kotlin 안정성 규칙

- Kotlin non-null assertion(`!!`)은 사용하지 않는다.
- nullable 값은 `?: throw ...`, `requireNotNull`, `checkNotNull`, 명시적 early return 등으로 의도를 드러내며 처리한다.
- 플랫폼 타입이나 외부 라이브러리 반환값은 null 가능성을 가정하고 명시적으로 검증한다.
- 트랜잭션 블록 안에는 DB 상태 변경만 두고, 외부 캐시/인메모리 저장소/HTTP 호출 같은 롤백 불가능한 작업은 커밋 성공 이후 수행한다.

## PRE배차 API

- 엔드포인트: `POST /dispatch/pre`
- REST API 경로는 `DispatchApiPaths`에 상수화한다.
- 카카오모빌리티 API 경로는 `KakaoMobilityApiPaths`에 상수화한다.
- PRE배차 요청은 출발지와 목적지를 받는다. 사용자 ID는 Gateway가 검증 후 주입한 `X-Authenticated-User-Id` 헤더를 사용한다.
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
  - `INTERNAL_API_KEY`
- relocation-service 주요 환경변수:
  - `INTERNAL_API_KEY`
- user-service 주요 환경변수:
  - `JWT_SECRET`
- gateway-service 주요 환경변수:
  - `GATEWAY_PORT`
  - `JWT_SECRET`
  - `USER_SERVICE_URL`
  - `DISPATCH_SERVICE_URL`
  - `CONTROL_SERVICE_URL`
  - `RELOCATION_SERVICE_URL`

## 테스트와 메시지

- 테스트 함수명은 한국어로 작성한다.
- 사용자에게 노출될 수 있는 에러 메시지는 한국어로 작성한다.
- JSON 필드명, DB 컬럼명, 외부 API 파라미터명처럼 프로토콜에 속한 문자열은 기존 표기를 유지한다.
- JSON 응답 문자열을 직접 하드코딩하지 말고, 가능하면 DTO/응답 모델과 Jackson/ObjectMapper를 통해 직렬화한다.
- common 모듈은 테스트가 없는 것을 정석으로 보지 않는다.
- 단순 예외 타입, DTO, 설정 골격만 있을 때는 테스트를 생략할 수 있다.
- 공통 응답 포맷, Jackson 설정, 예외 응답, 외부 API 클라이언트처럼 여러 서비스의 계약에 영향을 주는 코드는 우선적으로 테스트한다.
- `common-kakao` 테스트는 실제 카카오 API를 호출하지 않고 Mock HTTP 방식으로 요청 경로, 헤더, 쿼리 파라미터, 에러 매핑을 검증한다.

## CI

- GitHub Actions는 변경 감지 기반으로 필요한 모듈의 빌드/테스트를 수행한다.
- 현재 workflow에는 dev ECS 배포 job이 존재한다. 새 배포 대상 추가는 별도 인프라 합의 없이 하지 않는다.
- 변경 대상이 아닌 서비스 workflow는 실행하지 않아 PR checks에 표시하지 않는다.
- 루트 Gradle 설정, Gradle Wrapper가 바뀌면 서비스 빌드를 모두 수행한다.
- 서비스별 CI workflow가 바뀌면 해당 서비스 빌드를 수행한다.
- `common-core`, `common-web`이 바뀌면 4개 서비스 빌드를 모두 수행한다.
- `common-kakao`가 바뀌면 `dispatch-service`, `relocation-service` 빌드를 수행한다.
- `gateway-service`가 바뀌면 `gateway-service` 빌드를 수행한다.
- 서비스 모듈만 바뀌면 해당 서비스 빌드만 수행한다.
- 각 서비스 빌드는 `./gradlew :{service}:clean :{service}:build` 형태로 실행한다.
- dev ECS 배포 대상에 `gateway-service`를 포함한다. 단, 새 배포 대상 추가/변경은 `baro-terraform`의 ECR/ECS/ALB 리소스와 함께 맞춘다.

## 커밋 메시지

- 형식: `{작업유형}: {작업내용}`
- 작업유형은 영어로 작성한다.
- 작업내용은 한국어로 간략하게 작성한다.
- 예: `feat: PRE배차 DTO 추가`
