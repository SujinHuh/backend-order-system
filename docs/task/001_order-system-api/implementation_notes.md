# Implementation Notes

## 진행 로그

### 2026-05-01

- 하네스 downstream 문서 세트를 `backend-order-system`에 설치했다.
- README의 주문 시스템 요구사항을 기준으로 프로젝트 overlay 문서를 작성했다.
- 첫 구현 task `001_order-system-api`를 생성했다.
- 제출용 저장소에서는 하네스 실행 도구를 제외하고, 상태 전이와 재고 동시성 정책을 README와 architecture 문서에 직접 남기는 방향으로 정리했다.

### 2026-05-02

- Phase 1 planning gate에서 DB 모델 상세가 구현 가능한 수준인지 재검토했다.
- `architecture.md`에 `products`, `orders`, `order_items` 테이블 모델, 연관관계, 제약, 인덱스 기준을 보강했다.
- task `requirements.md`와 `plan.md`에 JPA entity/table 매핑, 감사 필드, 주문 당시 가격 보존, N+1 방지, lock 조회 테스트 기준을 추가했다.
- 독립 감사에서 주문 상태 변경 자체의 동시성 정책, 카테고리 enum 값, 동일 상태 변경 정책, 주문 목록 기간 조건 경계가 보강 필요 항목으로 확인됐다.
- 사용자 승인 후 README, architecture, task requirements/plan, DEC-001, DEC-002에 주문 row 비관적 락, 동일 상태 변경 실패 처리, 초기 지원 카테고리, 주문 목록 조건 형식을 반영했다.
- decision 문서 반영이 포함되어 `phase_status.md`의 allowed write-set에 `docs/project/decisions/*`를 추가했다.
- 기능별 이슈 분리 감사 결과를 반영해 QueryDSL은 스캐폴딩에서 의존성/annotation processor/Q-class 생성 확인까지만 담당하고, 실제 custom 조회 구현은 `feat/query-apis`에서 수행하도록 `plan.md` 범위를 정리했다.
- `001_order-system-api`는 전체 과제의 상위 마스터 task로 유지하고, 실제 구현은 `002_scaffold-common-setup`부터 기능별 task workspace에서 진행하기로 정리했다.
- `docs/project/standards/implementation_order.md`에도 QueryDSL custom repository 구현 범위를 `feat/query-apis` task로 분리한다는 단서를 추가했다.

## 구현 중 결정 기록

- 아직 코드 구현 전이다.
- DB 모델은 `Product`, `Order`, `OrderItem` 세 entity를 중심으로 구현한다.
- `Order`와 `Product`는 직접 다대다로 연결하지 않고 `OrderItem`을 통해 연결한다.
- `Category`와 `OrderStatus`는 enum으로 두고 DB에는 문자열로 저장한다.
- `Category` 초기 지원 enum 값은 `FOOD`, `FASHION`, `ELECTRONICS`, `BOOK`, `ETC`로 제한한다.
- 주문 당시 가격은 `OrderItem.orderPrice`에 저장한다.
- 생성일시와 수정일시는 JPA auditing 기반 공통 감사 필드로 관리한다.
- 주문 상태 변경 API는 대상 주문을 비관적 쓰기 락으로 조회한 뒤 상태 전이 가능 여부를 검증한다.
- 동일 상태 변경 요청은 멱등 성공이 아니라 허용되지 않은 주문 상태 변경 예외로 응답한다.
- 주문 목록 조회의 `from`, `to`는 ISO local date-time 형식이며 inclusive 경계로 해석한다.

## 계획 대비 변경

- Phase 1 문서 보강으로 DB 모델 상세 설계, 연관관계, 인덱스 고려사항, 주문 상태 변경 동시성 정책을 명시했다.

## 검증 기록

- 아직 코드 구현 전이라 `./mvnw test`는 실행 대상이 아니다.
