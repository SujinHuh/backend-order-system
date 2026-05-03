# Plan

## 변경 대상 파일 또는 모듈

- `src/main/java/com/example/ordersystem/order/domain/Order.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderItem.java`
- `src/main/java/com/example/ordersystem/order/service/OrderService.java`
- `src/main/java/com/example/ordersystem/order/controller/OrderController.java`
- `src/main/java/com/example/ordersystem/order/controller/dto/OrderStatusUpdateRequest.java`
- `src/test/java/com/example/ordersystem/order/domain/OrderStateTest.java`
- `src/test/java/com/example/ordersystem/order/service/OrderStockIntegrationTest.java`

## 레이어별 작업 계획

### 1. 도메인 로직 고도화
- `Order` 엔티티에 상태 전이 메서드 구현:
    - `changeStatus(targetStatus)`: `DEC-001` 규칙에 따른 분기 처리.
    - `COMPLETED` 전환 시 주문 항목 순회하며 `item.deductStock()` 호출.
    - `CANCELED` 전환 시 이전 상태가 `COMPLETED`였다면 `item.restoreStock()` 호출.
- `OrderItem` 엔티티에 재고 연동 메서드 추가:
    - `deductStock()`: 상품 재고 차감 지시.
    - `restoreStock()`: 상품 재고 복구 지시.
- **단위 테스트** 작성: 상태별 전이 성공/실패 및 재고 메서드 호출 여부 검증.

### 2. Service 레이어
- `OrderService.updateStatus(id, targetStatus)` 구현.
- 주문 조회 후 도메인 메서드 호출 및 트랜잭션 완료.
- **통합 테스트** 작성: DB에 반영된 실제 상품 재고 값 비교 검증.

### 3. Controller 및 DTO
- `PATCH /api/orders/{id}/status` 엔드포인트 추가.
- `OrderStatusUpdateRequest` DTO 정의 및 Validation.
- **슬라이스 테스트** 작성.

## 테스트 계획

- **성공 케이스**: PENDING -> ACCEPTED -> COMPLETED(재고감소) -> CANCELED(재고복구) 전체 흐름.
- **실패 케이스**: 재고 부족 시 COMPLETED 전환 실패, CANCELED 주문 상태 변경 시도 등.

## 비범위

- 비관적 락을 통한 동시성 제어 (Task 007).

## 리스크 또는 확인 포인트

- `OrderItem`이 `Product` 엔티티를 가지고 있으므로, 연쇄적인 재고 변경이 안정적으로 일어나는지 확인.
- 동일 상태 변경 요청 시 예외 처리 로직 작동 여부.
