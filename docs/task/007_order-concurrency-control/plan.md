# Plan

## 변경 대상 파일 또는 모듈

- `src/main/java/com/example/ordersystem/order/domain/OrderRepository.java`
- `src/main/java/com/example/ordersystem/product/domain/ProductRepository.java`
- `src/main/java/com/example/ordersystem/order/domain/Order.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderItem.java`
- `src/main/java/com/example/ordersystem/order/service/OrderService.java`
- `src/test/java/com/example/ordersystem/order/service/OrderConcurrencyIntegrationTest.java`
- `src/test/java/com/example/ordersystem/order/service/OrderStockIntegrationTest.java`
- `docs/task/007_order-concurrency-control/*`

## 레이어별 작업 계획

### 1. Repository

- `OrderRepository`에 주문 row 비관적 쓰기 락 조회 메서드를 추가한다.
- `ProductRepository`에 상품 ID 목록을 비관적 쓰기 락으로 조회하는 메서드를 추가한다.
- 상품 목록 조회 결과가 상품 ID 오름차순으로 반환되도록 쿼리 또는 service 정렬을 명시한다.

### 2. Domain

- `Order`가 현재 상태와 대상 상태 기준으로 재고 변경 필요 여부를 판단할 수 있는 메서드를 제공한다.
- 주문 항목의 상품 ID 목록을 추출할 수 있는 메서드를 제공한다.
- 기존 `changeStatus`의 상태 전이와 재고 차감/복구 정책은 유지한다.
- 필요한 경우 `OrderItem`에 product ID 접근 메서드를 추가한다.

### 3. Service

- `OrderService.updateStatus`에서 일반 `findById` 대신 주문 row 락 조회를 사용한다.
- 재고 변경이 필요한 상태 전이인 경우 상품 ID를 정렬해 상품 row 락을 획득한다.
- 락 획득 후 `order.changeStatus(targetStatus)`를 호출해 기존 도메인 정책으로 상태 전이와 재고 변경을 수행한다.
- 재고 변경이 없는 상태 전이는 상품 row 락 없이 기존 흐름을 유지한다.

### 4. Test

- 동일 상품에 대해 여러 주문을 동시에 `COMPLETED`로 변경하는 통합 테스트를 추가한다.
- 재고보다 많은 동시 완료 요청에서 성공 수만큼만 재고가 차감되고 최종 재고가 음수가 아님을 검증한다.
- 같은 주문에 대해 동시에 `COMPLETED` 변경 요청을 수행해 재고 중복 차감이 발생하지 않음을 검증한다.
- 기존 순차 재고 차감/복구 테스트가 계속 통과하는지 확인한다.

## 테스트 계획

- Repository 또는 service 통합 테스트에서 비관적 락 기반 상태 변경 흐름을 검증한다.
- 동시성 테스트는 `ExecutorService`, `CountDownLatch`, 별도 트랜잭션 커밋 경계를 사용해 실제 동시 요청에 가깝게 구성한다.
- 테스트 DB는 기본 H2를 사용한다.
- 최종 품질 게이트로 `./mvnw test`를 실행한다.

## 문서 반영 계획

- Phase 2 진행 중 레이어별 구현 결과와 위임 책임을 `implementation_notes.md`에 기록한다.
- Phase 4에서 실행 검증과 미실행 검증을 `validation_report.md`에 기록한다.
- Phase 5에서 `DEC-002`와 실제 구현이 일치하는지 확인한다.
- `DEC-002`의 정책을 변경하지 않으므로 새 decision 문서는 만들지 않는다.
- README의 동시성 설명은 `docs/final-readme`에서 최종 정리하되, 이번 구현 결과와 불일치가 있으면 Phase 5에서 반영 후보로 남긴다.

## 비범위

- QueryDSL 조건 조회 및 주문 목록 조회 API 구현
- 상품 카테고리 조건 조회 구현
- 최종 README 제출본 작성
- 동시성 전략을 낙관적 락 또는 조건부 update로 변경
- 주문 생성 시점 재고 예약 도입
- 전역 예외 코드 체계 개편

## 리스크 또는 확인 포인트

- H2의 비관적 락 동작이 실제 운영 DB와 완전히 같지는 않으므로, 테스트는 프로젝트 기본 환경에서 가능한 정합성 검증으로 제한한다.
- `@Transactional` 테스트 메서드 안에서 동시 작업을 실행하면 각 스레드의 트랜잭션 경계가 흐려질 수 있으므로 동시성 테스트는 테스트 메서드 트랜잭션을 피한다.
- 주문 row와 상품 row를 모두 락으로 잡기 때문에 락 획득 순서를 어기면 deadlock 가능성이 커진다.
- `Order.changeStatus` 내부에서 이미 연결된 `Product` 엔티티의 재고를 변경하므로, 상품 락 조회가 같은 persistence context의 엔티티와 어떻게 정합되는지 확인해야 한다.

## Phase 1 내부 감사 결과

### Requirements Audit

- `issue.md`의 요청사항, 비범위, 제약 조건이 `requirements.md`에 반영되었다.
- 비관적 락 적용 대상, 락 획득 순서, 트랜잭션 범위, 예외 상황, 성공 기준이 구현 가능한 수준으로 구체화되었다.
- 동시성 전략은 기존 `DEC-002`를 따르므로 추가 사용자 질의 없이 Phase 2 계획 수립이 가능하다.
- 판정: 승인 가능

### Plan Audit

- `plan.md`는 repository, domain, service, test 순서로 요구사항을 구현할 변경 대상과 작업 순서를 포함한다.
- 테스트 계획, 문서 반영 계획, 비범위, 리스크가 포함되었다.
- `DEC-002` 읽기/현행화 판단 계획이 문서 반영 계획에 포함되었다.
- Query API, final README, 예외 코드 개편 같은 범위 밖 작업은 제외되었다.
- 판정: 승인 가능

### Issue 대비 Plan 누락 감사

- 주문 row 락, 상품 row 락, 상품 ID 정렬 락 획득, 하나의 트랜잭션 처리, 동시 주문 완료 테스트, 같은 주문 중복 완료 테스트가 계획에 반영되었다.
- `issue.md`의 비범위가 `plan.md`에서도 유지되었다.
- `DEC-001`, `DEC-002`, service 트랜잭션, controller-repository 직접 호출 금지 제약이 계획에 반영되었다.
- 판정: 승인 가능
