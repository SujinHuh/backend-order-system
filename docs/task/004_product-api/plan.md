# Plan

## 변경 대상 파일 또는 모듈

- `src/main/java/com/example/ordersystem/product/domain/Product.java`
- `src/main/java/com/example/ordersystem/product/domain/Category.java`
- `src/main/java/com/example/ordersystem/product/domain/ProductRepository.java`
- `src/main/java/com/example/ordersystem/product/service/ProductService.java`
- `src/main/java/com/example/ordersystem/product/controller/ProductController.java`
- `src/main/java/com/example/ordersystem/product/controller/dto/*`
- `src/test/java/com/example/ordersystem/product/domain/ProductTest.java`
- `src/test/java/com/example/ordersystem/product/service/ProductServiceTest.java`
- `src/test/java/com/example/ordersystem/product/controller/ProductControllerTest.java`

## 레이어별 작업 계획

### 1. 도메인 및 엔티티 구현
- `Category` Enum 구현 (FOOD, FASHION 등)
- `Product` 엔티티 구현: Domain-First 패키지(`product.domain`) 적용
- 비즈니스 로직(가격 검증, 재고 관리) 구현 및 **단위 테스트** 작성

### 2. Repository 레이어
- `ProductRepository` 인터페이스 생성 (Spring Data JPA)

### 3. Service 레이어
- `ProductService` 구현: 등록, 수정, 단건 조회, 페이징 목록 조회
- 트랜잭션 관리 및 예외 처리 연동
- **통합 테스트** 작성

### 4. Controller 및 DTO
- `ProductController` 구현: `/api/products` 엔드포인트 매핑
- `Pageable`을 이용한 목록 조회 구현
- Request/Response DTO 정의 및 Validation 적용
- **슬라이스 테스트** 작성

## 테스트 계획

- **단위 테스트**: `Product` 엔티티의 비즈니스 메서드 (재고 부족 상황 등)
- **Service 테스트**: DB와 연동하여 실제 CRUD 기능 검증
- **API 테스트**: MockMvc를 사용하여 HTTP 요청/응답 및 Validation 검증

## 문서 반영 계획

- `README.md`에 상품 API 명세 업데이트 (선택 사항)

## 비범위

- QueryDSL Custom Repository (Task 008 예정)
- 동시성 제어 (Task 007 예정)

## 리스크 또는 확인 포인트

- `BigDecimal` vs `Long` 가격 타입 결정 (소수점 필요 없으면 Long 권장)
- `@EnableJpaAuditing` 설정이 `JpaAuditConfig`로 잘 분리되었는지 확인
