# Requirements

## 1. 주문 상태 전이 규칙 (State Machine)
- **허용 전이 목록**:
    - `PENDING` -> `ACCEPTED`: 접수 확인
    - `ACCEPTED` -> `COMPLETED`: 배송/주문 완료 (재고 차감 실행)
    - `PENDING` -> `CANCELED`: 대기 중 취소
    - `ACCEPTED` -> `CANCELED`: 접수 후 취소
    - `COMPLETED` -> `CANCELED`: 완료 후 취소 (재고 복구 실행)
- **금지 전이**:
    - 동일 상태로의 변경 (예: `ACCEPTED` -> `ACCEPTED`)
    - `CANCELED` 상태에서의 모든 변경
    - 정의되지 않은 모든 경로 (예: `PENDING` -> `COMPLETED`)
- **실패 시**: `BusinessException` (400) 발생

## 2. 재고 처리 정책 (Stock Policy)
- **차감 시점 (`COMPLETED` 전환 시)**:
    - 모든 주문 항목(`OrderItem`)의 상품 재고를 차감.
    - `Product.removeStock(quantity)` 호출.
    - 재고 부족 시 `NotEnoughStockException` 발생 -> 트랜잭션 롤백.
- **복구 시점 (`COMPLETED` -> `CANCELED` 전환 시)**:
    - 차감되었던 모든 상품 재고를 다시 복구.
    - `Product.addStock(quantity)` 호출.
- **무시 시점**: `PENDING/ACCEPTED` 상태에서 `CANCELED` 전환 시에는 재고를 건드리지 않음.

## 3. 주문 상태 변경 API
- **Endpoint**: `PATCH /api/orders/{id}/status`
- **Request Body**:
    ```json
    {
      "status": "COMPLETED"
    }
    ```
- **Response Body**: 변경된 주문 상세 정보 (`OrderResponse`)

## 성공 기준
- 상태 전이가 규칙에 따라 정확히 수행됨.
- `COMPLETED` 시점에만 실제 DB 재고가 감소함.
- `COMPLETED`에서 `CANCELED`로 갈 때만 DB 재고가 증가함.
- 잘못된 전이 시도 시 400 에러와 함께 데이터가 보존됨.
