# DEC-002 상품 재고 동시성 처리 정책

- Status: Accepted
- Type: Architecture
- Date: 2026-05-01
- Related Docs: README.md, docs/project/standards/architecture.md, docs/project/standards/testing_profile.md, docs/task/001_order-system-api/requirements.md
- When To Update: 재고 동시성 전략 또는 재고 차감 트랜잭션 범위가 바뀔 때

## Status

Accepted

## Context

여러 주문이 동시에 같은 상품의 재고를 차감하면 재고가 음수가 되거나 중복 차감될 수 있다. 과제는 여러 주문이 동시에 발생하더라도 상품 재고가 정확하게 차감되도록 구현하고, 동시성 해결 방식을 코드 또는 README에 설명하라고 요구한다.

## Decision

주문 상태 변경 API는 대상 주문 row에 비관적 쓰기 락을 적용한다. 재고 변경이 필요한 상태 전이에서는 재고 변경 대상 상품 row에도 비관적 락을 적용한다.

구현 방향은 다음과 같다.

- 주문 상태 변경 service 메서드에 `@Transactional`을 적용한다.
- 대상 주문을 조회할 때 `PESSIMISTIC_WRITE` 락을 사용한다.
- `COMPLETED` 전환이나 `COMPLETED -> CANCELED`처럼 재고 변경이 필요한 상태 전이에서는 주문 항목의 상품 ID 목록을 기준으로 상품을 조회할 때 `PESSIMISTIC_WRITE` 락을 사용한다.
- 주문 row 락을 먼저 획득한 뒤, 상품 ID 정렬 순서로 상품 row 락을 획득한다.
- 락을 획득한 뒤 상태 전이 가능 여부, 재고 수량, 재고 변경을 처리한다.
- 재고 차감과 주문 상태 변경을 같은 트랜잭션 안에서 완료한다.

## Rationale

과제 규모에서는 비관적 락이 재고 정합성을 설명하기 가장 명확하다. 같은 주문 row의 상태 변경을 직렬화해 동일 주문 중복 변경을 막고, 같은 상품 row를 변경하는 트랜잭션을 직렬화해 재고 부족 판단과 재고 변경을 안정적으로 처리할 수 있다.

## Consequences

- 같은 상품에 주문 완료 요청이 몰리면 해당 상품 row 기준으로 대기가 발생할 수 있다.
- 같은 주문에 상태 변경 요청이 몰리면 해당 주문 row 기준으로 대기가 발생할 수 있다.
- 재고 정합성을 우선한다.
- 락 범위를 줄이기 위해 트랜잭션 안에서 불필요한 외부 작업을 수행하지 않는다.
- 여러 상품을 동시에 차감할 때는 상품 ID 정렬 후 조회해 deadlock 가능성을 줄인다.

## Related Docs

- `README.md`
- `docs/project/standards/architecture.md`
- `docs/project/standards/testing_profile.md`
- `docs/task/001_order-system-api/requirements.md`

## When To Update

- 낙관적 락 또는 조건부 update 방식으로 동시성 전략을 변경할 때
- 주문 생성 시점에 재고 예약을 도입할 때
- 재고 차감 트랜잭션 범위가 바뀔 때
