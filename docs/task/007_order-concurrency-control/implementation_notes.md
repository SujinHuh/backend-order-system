# Implementation Notes

## 진행 로그

- Phase 1 시작:
  - `main`에서 `feat/order-concurrency-control` 브랜치를 생성하고 체크아웃했다.
  - `AGENTS.md`, `docs/entrypoint.md`, 공통 process guide, 프로젝트 overlay 문서, `DEC-001`, `DEC-002`를 확인했다.
  - 기존 `OrderService`, `Order`, `Product`, repository, `OrderStockIntegrationTest`, `docs/task/006_order-status-stock` 산출물을 확인했다.
- Phase 1 산출물 작성:
  - `issue.md`에 동시성 제어 배경, 요청사항, 비범위, 제약 조건을 정리했다.
  - `requirements.md`에 주문 row 락, 상품 row 락, 트랜잭션 처리 순서, 예외 상황, 성공 기준을 정의했다.
  - `plan.md`에 repository -> domain -> service -> test 순서의 작업 계획과 테스트/문서 반영 계획을 작성했다.
  - Phase gate 정책에 따라 `validation_report.md`는 Phase 4까지 생성하지 않는다.
- Phase 1 내부 감사:
  - requirements audit: 승인 가능
  - plan audit: 승인 가능
  - issue 대비 plan 누락 감사: 승인 가능
  - 현재 상태는 Phase 1 사용자 승인 대기다.
- Phase 2 시작:
  - 사용자 지시에 따라 Phase 2 TDD Implementation으로 이동했다.
  - 선택 레이어 순서는 `plan.md`와 `implementation_order.md` 기준으로 repository -> domain -> service -> test 연결 검증이다.
  - 실제 비관적 락과 동시 트랜잭션 정합성은 단위 테스트 더블로 검증하기 부적절하므로 Spring/JPA 통합 테스트를 먼저 작성한다.
- Phase 2 TDD 실행:
  - 실패 테스트 추가: `OrderConcurrencyIntegrationTest`
    - 같은 상품에 대한 동시 주문 완료 요청은 성공한 주문 수만큼만 재고를 차감해야 한다.
    - 같은 주문에 대한 동시 완료 요청은 재고를 중복 차감하지 않아야 한다.
  - 기존 `OrderServiceTest`는 `updateStatus`가 주문 row lock 조회를 사용하도록 기대값을 갱신했다.
  - 최초 실행 결과:
    - `OrderRepository.findByIdForUpdate` 미구현으로 컴파일 실패
    - 테스트 record accessor 이름 충돌로 컴파일 실패
  - 최소 구현:
    - `OrderRepository.findByIdForUpdate`에 `PESSIMISTIC_WRITE`와 `orderItems` EntityGraph 적용
    - `ProductRepository.findAllByIdInOrderByIdAsc`에 `PESSIMISTIC_WRITE` 적용
    - `Order.requiresStockLock`, `Order.getOrderItemProductIds`, `OrderItem.getProductId` 추가
    - `OrderService.updateStatus`에서 주문 row lock 후 필요한 경우 상품 row lock을 획득하도록 변경
  - 중간 실패:
    - 주문 락 조회에서 `orderItems.product`까지 먼저 로딩해 동시 상품 재고 차감 테스트가 실패했다.
    - 상품 엔티티는 별도 상품 락 쿼리에서 로딩되도록 `EntityGraph`를 `orderItems`로 축소했다.
  - 대상 테스트 통과:
    - `./mvnw test -Dtest=OrderServiceTest,OrderConcurrencyIntegrationTest`
    - Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
  - 전체 테스트 통과:
    - `./mvnw test`
    - Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
- Phase 2 자체 감사:
  - TDD 순서: 실패 테스트 작성 후 최소 구현으로 통과시켰다.
  - 범위 통제: Query API, README, 예외 코드 개편은 건드리지 않았다.
  - 테스트 더블: 동시성/락 정합성은 통합 테스트로 검증했고, 기존 service 단위 테스트만 mock 기반 기대값을 갱신했다.
  - 레이어 순서: repository -> domain helper -> service -> 통합 테스트 검증 순서로 `implementation_order.md`와 충돌하지 않는다.
  - 품질 게이트: `./mvnw test` 통과.
  - 판정: 승인 가능
- Phase 3 시작:
  - 사용자 지시에 따라 Phase 3 Integration으로 이동했다.
  - Phase 2에서 위임한 책임은 실제 동시 실행/락 정합성이다.
  - 통합 검증 대상은 `OrderConcurrencyIntegrationTest`와 기존 `OrderStockIntegrationTest`다.
  - 내부 협력 객체를 mock으로 대체하지 않고 Spring Boot, JPA repository, H2 DB, service 트랜잭션을 실제 조립으로 검증한다.
- Phase 3 통합 검증:
  - `./mvnw test -Dtest=OrderConcurrencyIntegrationTest,OrderStockIntegrationTest`
    - Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
  - 검증한 연결 책임:
    - `OrderService.updateStatus` -> `OrderRepository.findByIdForUpdate` 주문 row lock 조회
    - `OrderService.updateStatus` -> `ProductRepository.findAllByIdInOrderByIdAsc` 상품 row lock 조회
    - JPA repository, service transaction, H2 DB 조립 상태에서 동시 요청 처리
  - 핵심 happy path:
    - 순차 상태 변경에서 완료 시 재고 차감, 완료 주문 취소 시 재고 복구
  - 주요 failure path:
    - 같은 상품에 재고보다 많은 동시 완료 요청이 들어오면 성공한 주문 수만큼만 차감되고 나머지는 재고 부족으로 실패
    - 같은 주문에 동시 완료 요청이 들어오면 1건만 성공하고 나머지는 상태 전이 오류로 실패
  - 전체 품질 게이트:
    - `./mvnw test`
    - Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
- Phase 3 감사:
  - Phase 2에서 위임된 실제 동시 실행/락 정합성 책임이 통합 테스트로 검증되었다.
  - 내부 협력 객체를 mock으로 대체하지 않아 실제 연결 책임이 유지되었다.
  - 핵심 happy path와 주요 failure path가 통합 수준에서 검증되었다.
  - 필수 test gate가 실행되었다.
  - H2 기반 검증이 운영 DB 락 특성을 완전히 대체하지 못하는 잔여 리스크는 Phase 4에서 기록한다.
  - 판정: 승인 가능
- Phase 4 Validation:
  - `validation_report.md`를 생성해 자동 검증, 수동 검증, 미실행 검증, 결과 요약, related decisions 판단, 잔여 리스크를 기록했다.
  - Phase 1 요청사항, 제약사항, 비범위 대비 누락 또는 범위 확장은 확인되지 않았다.
  - `OrderStockIntegrationTest`는 계획에 변경 대상으로 포함됐지만 실제 수정은 필요 없었고, 재실행으로 기존 순차 재고 흐름을 검증했다.
  - Phase 4 감사 판정은 승인 가능이다.
- Phase 5 Documentation:
  - 최신 `validation_report.md`의 `Phase 5에서 반영할 related decisions/`를 확인했다.
  - 새 decision 생성은 필요 없다. 이번 작업은 기존 `DEC-002`의 비관적 락 정책을 구현한 것이며 전략 자체를 변경하지 않았다.
  - `DEC-002`는 현재 구현 결과와 정책이 일치하므로 수정하지 않는다.
  - README 최종 정리는 이번 태스크 비범위이며 `docs/final-readme` 후속 태스크에서 처리한다.
  - close-out 결과와 확인 문서 위치를 task-local 문서에 남긴다.

## 경량 검토 기록

- 작은 태스크로 본 근거: 해당 없음. 동시성 제어는 핵심 비즈니스 정합성 작업이므로 full Phase 절차를 따른다.
- 경량 적용 승인 여부: 해당 없음
- 실제 축소한 범위: 해당 없음
- 유지한 테스트: Phase 2/3에서 동시성 통합 테스트와 `./mvnw test` 유지 예정
- 유지한 감사: Phase 1 내부 감사 3종 유지
- 전체 흐름 영향 요약: 주문 상태 변경과 재고 변경 트랜잭션 경계를 건드리므로 full 검증 필요
- 남은 리스크: H2 기반 동시성 테스트가 실제 운영 DB 락 특성을 완전히 대체하지는 못함
- Full 전환 조건 또는 승격 조건: 이미 full 절차 적용

## 구현 중 결정 사항

- repo-local 근거:
  - `docs/project/decisions/DEC-001-order-state-and-stock-policy.md`
  - `docs/project/decisions/DEC-002-stock-concurrency-policy.md`
  - `docs/project/standards/architecture.md`
  - `docs/project/standards/testing_profile.md`
  - `docs/task/006_order-status-stock/*`
- repo에 없어 문서화/승인 대상으로 넘긴 결정:
  - 없음. 동시성 전략은 기존 `DEC-002`에 정의되어 있다.

## 위임된 책임

- Phase 2에서 단위 테스트로 검증하기 어려운 실제 동시 실행/락 정합성은 Phase 3 통합 테스트 검증 대상으로 유지한다.

## 사용자 승인 필요 항목

- Phase 5 close-out 확인 후 필요한 경우 커밋/푸시를 진행한다.

## 후속 태스크 후보

- `feat/query-apis`: 주문/상품 조건 조회 및 QueryDSL custom repository 구현
- `docs/final-readme`: 최종 README와 실제 구현 범위 정합성 정리
- 예외 코드 세분화: 재고 부족, 상태 전이 오류 등 도메인별 에러 코드 분리
