# Requirements

## 1. 도메인 모델 (Product)
- **속성**:
    - `id`: Long (Primary Key, Identity)
    - `name`: String (Not Null)
    - `price`: Long (Not Null, 0 이상)
    - `stockQuantity`: int (Not Null, 0 이상)
    - `category`: Category Enum (Not Null)
    - `createdAt`, `updatedAt`: Auditing 활용
- **비즈니스 로직**:
    - `changePrice(price)`: 가격 변경 시 0원 미만 검증 (인자가 음수일 경우 `InvalidValueException` 발생)
    - `addStock(quantity)`: 재고 추가 (인자가 음수일 경우 `InvalidValueException` 발생)
    - `removeStock(quantity)`: 재고 차감 (인자가 음수일 경우 `InvalidValueException`, 현재 재고보다 많이 차감하려 할 경우 `NotEnoughStockException` 발생)

## 2. 카테고리 (Category)
- Enum 상수: `FOOD`, `FASHION`, `ELECTRONICS`, `BOOK`, `ETC`

## 3. 상품 등록 API
- **Endpoint**: `POST /api/products`
- **Request Body**: `name`, `price`, `stockQuantity`, `category`
- **Response Body**: `id`, `name`, `price`, `stockQuantity`, `category`, `createdAt`, `updatedAt` (201 Created)
- **Validation**: 
    - 이름은 필수이며 공백 불가
    - 가격은 0 이상
    - 초기 재고는 0 이상
    - 유효한 카테고리값 필수

## 4. 상품 수정 API
- **Endpoint**: `PUT /api/products/{id}`
- **Request Body**: `name`, `price`, `stockQuantity`, `category`
- **Response Body**: `id`, `name`, `price`, `stockQuantity`, `category`, `createdAt`, `updatedAt` (200 OK)

## 5. 상품 조회 API
- **단건 조회**: `GET /api/products/{id}` (존재하지 않을 경우 404 반환)
- **목록 조회**: `GET /api/products?page=0&size=10`
    - Spring Data JPA의 `Pageable`을 활용한 페이징 처리 필수 적용
    - 기본 정렬: ID 역순

## 성공 기준
- 상품 등록, 수정, 조회가 정상적으로 동작함.
- 모든 비즈니스 규칙(음수 값 방어, 재고 부족 처리)이 단위 테스트로 검증됨.
- 목록 조회 시 페이징 정보가 포함된 응답이 반환됨.
