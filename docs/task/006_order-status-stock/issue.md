# Issue

## 배경

주문 시스템의 핵심 비즈니스 로직인 주문 상태 관리와 재고 처리 정책을 구현함. `DEC-001` 정책에 따라 주문 상태 전이에 따른 실제 재고 차감 및 복구 로직을 완성해야 함.

## 요청사항

- 주문 상태 변경 API 구현 (`PATCH /api/orders/{id}/status`)
- 허용된 상태 전이 규칙 구현:
    - `PENDING` -> `ACCEPTED` (단순 상태 변경)
    - `ACCEPTED` -> `COMPLETED` (**실제 재고 차감 발생**)
    - `PENDING` -> `CANCELED` (단순 상태 변경)
    - `ACCEPTED` -> `CANCELED` (단순 상태 변경)
    - `COMPLETED` -> `CANCELED` (**재고 복구 발생**)
- 상태 변경 시 정책 준수:
    - `COMPLETED` 전환 시 재고 부족이면 `NotEnoughStockException` 발생 및 상태 변경 실패.
    - 동일한 상태로의 변경 요청은 예외 처리.
    - 이미 `CANCELED`인 주문은 변경 불가.
- 도메인 모델 내 상태 전이 및 재고 연동 로직 구현.

## 비범위

- 비관적 락을 활용한 동시성 제어 (Task 007 예정).
- 배송 조회 등 외부 시스템 연동.

## 승인 또는 제약 조건

- `DEC-001` 정책을 엄격히 준수할 것.
- 상태 변경과 재고 처리는 하나의 트랜잭션 내에서 원자적으로 처리될 것.
- `OrderResponse` DTO를 활용하여 변경된 상태 정보를 반환할 것.
