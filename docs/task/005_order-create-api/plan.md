# Plan

## 변경 대상 파일 또는 모듈

- `src/main/java/com/example/ordersystem/order/domain/Order.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderItem.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderStatus.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderRepository.java`
- `src/main/java/com/example/ordersystem/order/service/OrderService.java`
- `src/main/java/com/example/ordersystem/order/controller/OrderController.java`
- `src/main/java/com/example/ordersystem/order/controller/dto/*`
- `src/test/java/com/example/ordersystem/order/domain/OrderTest.java`
- `src/test/java/com/example/ordersystem/order/service/OrderServiceTest.java`
- `src/test/java/com/example/ordersystem/order/controller/OrderControllerTest.java`

## 레이어별 작업 계획

### 1. 도메인 및 엔티티 구현
- `OrderStatus` Enum 구현
- `Order`, `OrderItem` 엔티티 구현: JPA 매핑 및 연관관계 설정
- 총 주문 금액 계산 등 도메인 로직 구현 및 **단위 테스트** 작성

### 2. Repository 레이어
- `OrderRepository` 생성 (Spring Data JPA)

### 3. Service 레이어
- `OrderService` 구현: 상품 조회, 주문 및 주문 항목 생성, 저장
- **통합 테스트** 작성 (상품 도메인과의 연동 확인)

### 4. Controller 및 DTO
- `OrderController` 구현: `POST /api/orders` 엔드포인트
- Request/Response DTO 정의 및 Validation 적용
- **슬라이스 테스트** 작성

## 테스트 계획

- **도메인 테스트**: 주문 생성 시 초기 상태 확인 및 총액 계산 로직 검증
- **서비스 테스트**: 실제 상품 데이터를 조회하여 주문이 정상 생성되는지 검증
- **API 테스트**: MockMvc를 이용한 요청 데이터 유효성 검증

## 비범위

- 재고 차감 로직 (다음 태스크인 Task 007에서 동시성 제어와 함께 고도화 예정)
- 주문 취소 및 상태 변경

## 리스크 또는 확인 포인트

- `Order`와 `OrderItem` 간의 양방향 연관관계 편의 메서드 구현 주의
- 상품 가격 변동에 영향받지 않도록 `orderPrice` 필드가 정확히 기록되는지 확인
