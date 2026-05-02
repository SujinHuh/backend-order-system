# Issue

## 배경

주문 시스템의 핵심 도메인인 상품(Product) 정보를 관리하기 위한 기능이 필요함. 상품 등록, 수정, 조회 기능을 제공하여 이후 주문 생성 단계에서 참조할 수 있도록 함.

## 요청사항

- `Product` 엔티티 및 `Category` Enum 구현
- 상품 등록 API (이름, 가격, 재고, 카테고리 포함)
- 상품 수정 API (이름, 가격, 재고, 카테고리 업데이트)
- 상품 단건 조회 API
- 상품 기본 목록 조회 API (페이징 없이 전체 조회 또는 기본 JPA 조회)
- 상품 가격 및 재고 수량의 음수 방지 비즈니스 로직 구현
- 재고 차감/복구 도메인 메서드 구현

## 비범위

- QueryDSL을 활용한 복잡한 조건 검색 (Task 008에서 진행)
- 상품 이미지 업로드 및 관리
- 상품 삭제 (Soft/Hard delete 모두 이번 범위 제외)

## 승인 또는 제약 조건

- `Category`는 `FOOD`, `FASHION`, `ELECTRONICS`, `BOOK`, `ETC`를 지원할 것.
- `products` 테이블 매핑 규칙을 준수할 것 (`id`, `name`, `price`, `stockQuantity`, `category`, `createdAt`, `updatedAt`).
- 도메인 모델에서 가격과 재고의 유효성을 검증할 것.
- `docs/project/standards/architecture.md`의 레이어 구조를 준수할 것.
