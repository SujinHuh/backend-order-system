# Requirements

## 1. 도메인 모델 (Product)
- **속성**:
    - `id`: Long (Primary Key, Identity)
    - `name`: String (Not Null)
    - `price`: BigDecimal 또는 Long (Not Null, 0 이상)
    - `stockQuantity`: int (Not Null, 0 이상)
    - `category`: Category Enum (Not Null)
    - `createdAt`, `updatedAt`: BaseEntity 또는 Auditing 활용
- **비즈니스 로직**:
    - `changePrice(price)`: 가격 변경 시 0원 미만 검증
    - `addStock(quantity)`: 재고 추가
    - `removeStock(quantity)`: 재고 차감 (현재 재고보다 많이 차감하려 할 경우 `NotEnoughStockException` 발생)

## 2. 카테고리 (Category)
- Enum 상수: `FOOD`, `FASHION`, `ELECTRONICS`, `BOOK`, `ETC`

## 3. 상품 등록 API
- **Endpoint**: `POST /api/products`
- **Request Body**: `name`, `price`, `stockQuantity`, `category`
- **Response**: 생성된 상품의 ID 및 상세 정보 (201 Created)
- **Validation**: 
    - 이름은 필수이며 공백 불가
    - 가격은 0 이상
    - 초기 재고는 0 이상
    - 유효한 카테고리값 필수

## 4. 상품 수정 API
- **Endpoint**: `PUT /api/products/{id}`
- **Request Body**: `name`, `price`, `stockQuantity`, `category`
- **Response**: 수정된 상품 상세 정보 (200 OK)
- **Validation**: 등록과 동일한 규칙 적용

## 5. 상품 조회 API
- **단건 조회**: `GET /api/products/{id}` (존재하지 않을 경우 404 반환)
- **목록 조회**: `GET /api/products` (전체 상품 목록 반환, 페이징은 이번 단계에서 생략 가능하나 기본 정렬은 ID 역순 추천)

## 성공 기준
- 상품 등록, 수정, 조회가 정상적으로 동작함.
- 가격이나 재고를 음수로 변경하려 할 경우 `BusinessException` (400)이 발생함.
- 재고 부족 상황에서 차감 시도 시 적절한 예외가 발생함.
- `createdAt`, `updatedAt`이 DB에 정상 기록됨.
