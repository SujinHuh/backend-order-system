# DEC-001 주문 상태 전이와 재고 처리 정책

- Status: Accepted
- Type: Architecture
- Date: 2026-05-01
- Related Docs: README.md, docs/project/standards/architecture.md, docs/project/standards/testing_profile.md, docs/task/001_order-system-api/requirements.md
- When To Update: 주문 상태, 재고 차감 시점, 완료 주문 취소 정책이 바뀔 때

## Status

Accepted

## Context

과제는 주문이 다음 상태를 가질 수 있다고 정의한다.

- 대기
- 접수
- 완료
- 취소

주문 생성 시 기본 상태는 대기이며, 비즈니스적으로 올바르지 않은 상태 변경은 허용하지 않아야 한다. 또한 주문 완료 시 재고가 차감되고, 주문 취소 시 재고가 복구되어야 한다.

## Decision

주문 상태는 다음 enum으로 구현한다.

- `PENDING`: 대기
- `ACCEPTED`: 접수
- `COMPLETED`: 완료
- `CANCELED`: 취소

허용 상태 전이는 다음으로 제한한다.

```text
PENDING -> ACCEPTED
ACCEPTED -> COMPLETED
PENDING -> CANCELED
ACCEPTED -> CANCELED
COMPLETED -> CANCELED
```

재고 정책은 다음과 같다.

- 주문 생성 시에는 재고를 차감하지 않는다.
- `COMPLETED`로 변경될 때 주문 항목 수량만큼 재고를 차감한다.
- `COMPLETED` 상태의 주문이 `CANCELED`로 변경될 경우 차감된 재고를 복구한다.
- `PENDING` 또는 `ACCEPTED` 상태에서 취소될 경우 차감된 재고가 없으므로 재고 복구를 수행하지 않는다.
- 이미 `CANCELED`인 주문은 다른 상태로 변경할 수 없다.

## Rationale

과제 문구에서 "주문이 완료가 되면 재고가 차감"된다고 명시되어 있으므로, 재고 차감 시점을 완료 전환으로 둔다. 동시에 "주문이 취소될 경우 재고 복구" 요구사항도 충족하기 위해 완료 주문의 취소를 허용하고, 이 경우에만 재고를 복구한다.

## Consequences

- 주문 생성은 빠르게 처리되며 재고를 선점하지 않는다.
- 실제 재고 부족 여부는 완료 전환 시 확정된다.
- 완료 후 취소가 가능하므로 재고 복구 로직과 테스트가 필요하다.
- 상태 변경과 재고 변경은 반드시 하나의 트랜잭션에서 처리해야 한다.

## Related Docs

- `README.md`
- `docs/project/standards/architecture.md`
- `docs/project/standards/testing_profile.md`
- `docs/task/001_order-system-api/requirements.md`

## When To Update

- 주문 상태가 추가되거나 제거될 때
- 재고 차감 시점이 주문 생성 또는 접수 시점으로 바뀔 때
- 완료 주문 취소 정책이 바뀔 때
