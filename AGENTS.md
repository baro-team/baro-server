# AGENTS.md

## 작업 범위

- dispatch-service는 DDD 구조로 작업한다.
- control-service 등 다른 서비스는 명시 요청이 없는 한 수정하지 않는다.
- 배포(CD)는 아직 추가하지 않는다.

## dispatch-service 패키지 구조

- `domain`: 도메인 모델, 도메인 예외, repository 계약을 둔다.
- `application`: 유스케이스 서비스와 외부 연동 port를 둔다.
- `infrastructure`: DB, 외부 API, 설정 등 기술 구현체를 둔다.
- `interfaces.rest`: REST controller, REST DTO, REST 예외 핸들러를 둔다.

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

## CI

- GitHub Actions CI는 정상 빌드와 테스트 통과만 확인한다.
- `./gradlew clean build`를 사용한다.
- CD 단계는 추가하지 않는다.

## 커밋 메시지

- 형식: `{작업유형}: {작업내용}`
- 작업유형은 영어로 작성한다.
- 작업내용은 한국어로 간략하게 작성한다.
- 예: `feat: PRE배차 DTO 추가`
