# Issue

## 배경

주문 시스템의 핵심 기능인 주문 생성 기능이 필요함. 고객이 선택한 상품들과 수량을 바탕으로 주문을 생성하고, 주문 당시의 상품 가격을 기록하여 히스토리를 보존함.

## 요청사항

- `Order` 및 `OrderItem` 엔티티 구현
- `OrderStatus` Enum 구현 (`PENDING`, `ACCEPTED`, `COMPLETED`, `CANCELED`)
- 주문 생성 API (`POST /api/orders`) 구현
- 주문 생성 시 초기 상태는 `PENDING`으로 설정
- 주문 항목(`OrderItem`) 생성 시 상품의 현재 가격을 `orderPrice`로 기록
- 주문 생성 시에는 **재고를 차감하지 않음** (`architecture.md` 정책 준수)
- 최소 하나 이상의 주문 항목이 포함되어야 함

## 비범위

- 주문 상태 변경 및 재고 차감/복구 (Task 006, 007에서 진행)
- 주문 조회 API (Task 008에서 진행)
- 동시성 제어 적용

## 승인 또는 제약 조건

- `docs/project/standards/architecture.md`의 주문 상태 및 재고 정책을 엄격히 준수할 것.
- 주문 항목의 수량은 1개 이상이어야 함.
- 존재하지 않는 상품에 대한 주문 요청 시 404 에러를 반환할 것.
- 모든 응답은 DTO를 통해 제공할 것.
