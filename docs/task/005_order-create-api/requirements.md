# Requirements

## 1. 도메인 모델 (Order & OrderItem)
- **Order**:
    - `id`: Long (PK)
    - `status`: OrderStatus (Enum: PENDING, ACCEPTED, COMPLETED, CANCELED)
    - `orderItems`: List<OrderItem> (1:N 연관관계)
    - `createdAt`, `updatedAt`: Auditing 적용
- **OrderItem**:
    - `id`: Long (PK)
    - `order`: Order (N:1 연관관계)
    - `product`: Product (N:1 연관관계)
    - `quantity`: int (1 이상)
    - `orderPrice`: Long (주문 시점의 상품 가격)
- **OrderStatus**:
    - `PENDING`: 초기 생성 상태

## 2. 주문 생성 로직
- 상품 ID 목록과 수량을 입력받아 주문을 생성한다.
- 각 상품의 현재 가격을 조회하여 `OrderItem`에 기록한다.
- 주문 생성 시 총 주문 금액을 계산할 수 있어야 한다. (도메인 메서드 제공)
- **중요**: `architecture.md`에 따라 주문 생성 시점에는 상품 재고를 검증하거나 차감하지 않는다.

## 3. 주문 생성 API
- **Endpoint**: `POST /api/orders`
- **Request Body**:
    ```json
    {
      "items": [
        { "productId": 1, "quantity": 2 },
        { "productId": 2, "quantity": 1 }
      ]
    }
    ```
- **Response Body**: 생성된 주문의 ID 및 기본 정보 (201 Created)
- **Validation**:
    - `items` 목록은 비어있을 수 없음
    - `quantity`는 1 이상

## 성공 기준
- 주문과 주문 항목이 DB에 정상적으로 저장됨.
- 주문 상태가 `PENDING`으로 올바르게 설정됨.
- 주문 항목에 주문 시점의 상품 가격이 정확히 기록됨.
- 존재하지 않는 상품 ID 포함 시 `EntityNotFoundException`이 발생함.
