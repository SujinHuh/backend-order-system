# Requirements

## 기능 요구사항

### 1. 최종 README 현행화

- `README.md`는 구현 완료된 주문 시스템의 목적과 핵심 기능을 설명한다.
- README의 문구는 구현 전 계획이 아니라 현재 구현 기준으로 작성한다.
- 상품, 주문, 주문 항목, 카테고리, 주문 상태 도메인을 실제 enum/API 명칭과 맞춰 설명한다.

### 2. API 문서화

- 상품 API를 문서화한다.
  - `POST /api/products`
  - `PUT /api/products/{id}`
  - `GET /api/products/{id}`
  - `GET /api/products`
  - `GET /api/products?category={category}`
- 주문 API를 문서화한다.
  - `POST /api/orders`
  - `PATCH /api/orders/{id}/status`
  - `GET /api/orders/{id}`
  - `GET /api/orders`
  - `GET /api/orders?status={status}&from={from}&to={to}`
- README는 paging query parameter 사용 가능성을 설명한다.
- `from`, `to`는 ISO local date-time 형식과 inclusive 조건임을 설명한다.

### 3. 정책 문서화

- 주문 상태 전이 정책을 실제 구현 기준으로 설명한다.
- 주문 생성 시 재고를 차감하지 않고, `COMPLETED` 전이 시 차감하며, `COMPLETED -> CANCELED` 전이 시 복구하는 정책을 설명한다.
- 동일 상태 변경 요청은 허용 상태 전이에 포함하지 않는다고 설명한다.
- 상태 변경과 재고 변경은 트랜잭션 안에서 처리된다고 설명한다.
- 동시성 제어는 주문 row와 상품 row 비관적 락, 상품 ID 정렬 기반 락 획득 순서를 기준으로 설명한다.

### 4. 예외와 응답 문서화

- 공통 에러 응답 형식을 실제 `ErrorResponse`/`ErrorCode` 흐름에 맞춰 설명한다.
- 존재하지 않는 엔티티, 잘못된 입력, 허용되지 않은 상태 변경, 재고 부족 등 대표 예외 상황을 설명한다.
- 새 에러 코드나 세분화 정책을 추가로 확정하지 않는다.

### 5. 실행 및 검증 문서화

- Java, Spring Boot, Maven, H2, QueryDSL 기반 프로젝트임을 설명한다.
- 실행 명령과 테스트 명령을 제공한다.
- 주요 테스트 범위를 정리한다.
- 최종 README 작업 검증 결과를 `validation_report.md`에 남긴다.

## 비기능 요구사항 또는 품질 요구사항

- README는 실제 구현과 모순 없어야 한다.
- README는 제출자가 프로젝트 범위, 실행 방법, 주요 설계 결정을 빠르게 이해할 수 있는 구조여야 한다.
- 구현되지 않은 운영 DB, migration, 부하 테스트, query count 자동 검증 등을 완료된 것처럼 표현하지 않는다.
- 문서 변경만 수행하되, 문서가 가리키는 코드/검증 근거를 task 산출물에 남긴다.

## 입력/출력

- 입력:
  - `README.md`
  - `docs/project/standards/*`
  - `docs/project/decisions/*`
  - `docs/task/001_order-system-api` ~ `docs/task/008_query-apis`
  - 실제 `src/main`, `src/test` 구현
- 출력:
  - 갱신된 `README.md`
  - `docs/task/final-readme/*` task 산출물

## 제약사항

- 기능 코드와 테스트 코드는 변경하지 않는다.
- README 외 canonical 프로젝트 문서는 새 결정이 없으면 변경하지 않는다.
- `README.md`에 구현되지 않은 API나 보장되지 않은 운영 특성을 추가하지 않는다.
- 이번 작업은 최종 제출 README 정리이며, 신규 기능 구현 task가 아니다.

## 예외 상황

- README와 실제 구현이 불일치하면 README를 실제 구현 기준으로 수정한다.
- 기존 프로젝트 표준 또는 decision 문서와 충돌하는 새 정책이 필요해지면 Phase 1/4/5에서 decision 반영 후보로 분리한다.
- 확인 불가능한 외부 제출 절차는 기존 README 범위를 넘겨 새로 확정하지 않는다.

## 성공 기준

- `README.md`가 실제 API, 정책, 실행/검증 방법을 정확히 설명한다.
- 완료된 001~008 task의 핵심 구현 결과가 README에 반영된다.
- 비범위 기능을 완료된 것처럼 설명하지 않는다.
- `./mvnw test`가 통과한다.
- `git diff --check`가 통과한다.
- phase gate 검증이 통과한다.

## Decision 반영 후보

- 현재 작업은 최종 README 문서화이며 새 구조적 결정이나 장기 정책 변경을 만들지 않는다.
- 기존 decision과 architecture를 설명하는 범위이므로 `docs/project/decisions/*` 수정은 계획하지 않는다.
- README 작성 중 새 정책 확정이 필요해지면 decision 반영 후보로 재분류한다.
