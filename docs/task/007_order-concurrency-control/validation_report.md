# Validation Report

## 실행한 검증

### 1. 자동 검증: 대상 통합 테스트

- 검증 항목: 동시성 제어 통합 검증
- 대조한 입력물: `issue.md`, `requirements.md`, `plan.md`, `implementation_notes.md`, `OrderConcurrencyIntegrationTest`, `OrderStockIntegrationTest`
- 실행 방법:

```bash
./mvnw test -Dtest=OrderConcurrencyIntegrationTest,OrderStockIntegrationTest
```

- 결과:
  - Tests run: 3
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- 확인 내용:
  - 같은 상품에 재고보다 많은 동시 완료 요청이 들어와도 성공한 주문 수만큼만 재고가 차감된다.
  - 같은 상품 동시 완료 요청 이후 최종 재고가 음수가 되지 않는다.
  - 같은 주문에 동시에 완료 요청이 들어와도 재고가 중복 차감되지 않는다.
  - 기존 순차 상태 변경에서 완료 시 재고 차감, 완료 주문 취소 시 재고 복구가 유지된다.
- 판정: `정합`
- 잔여 리스크: H2 기반 락 검증은 운영 DB의 모든 lock wait, timeout, deadlock 특성을 완전히 대체하지 않는다.

### 2. 자동 검증: 전체 품질 게이트

- 검증 항목: 컴파일 및 전체 테스트 회귀 검증
- 대조한 입력물: `quality_gate_profile.md`, 전체 구현 결과
- 실행 방법:

```bash
./mvnw test
```

- 결과:
  - Tests run: 40
  - Failures: 0
  - Errors: 0
  - Skipped: 0
- 확인 내용:
  - 전체 컴파일 성공
  - 기존 상품 API, 주문 생성, 주문 상태 변경, 재고 차감/복구, 전역 예외 테스트 회귀 없음
  - 새 동시성 통합 테스트 포함 전체 테스트 통과
- 판정: `정합`
- 잔여 리스크: 없음

### 3. 자동 검증: Phase gate

- 검증 항목: 현재 Phase write-set 위반 여부
- 대조한 입력물: `phase_status.md`, Git dirty paths
- 실행 방법:

```bash
python3 scripts/validate_phase_gate.py docs/task/007_order-concurrency-control --git-scope repo
```

- 결과:
  - phase gate validation passed
- 확인 내용:
  - Phase 4에서 허용한 task 문서와 구현/테스트 파일 외 변경 없음
- 판정: `정합`
- 잔여 리스크: 없음

### 4. 자동 검증: diff whitespace

- 검증 항목: whitespace error 확인
- 대조한 입력물: Git diff
- 실행 방법:

```bash
git diff --check
```

- 결과:
  - 출력 없음
  - exit code 0
- 확인 내용:
  - trailing whitespace, conflict marker, whitespace error 없음
- 판정: `정합`
- 잔여 리스크: 없음

## 수동 검증

### 1. Phase 1 요청사항 대비 구현 결과

- 검증 항목: `issue.md` 요청사항 반영 여부
- 대조한 입력물: `issue.md`, `OrderService`, `OrderRepository`, `ProductRepository`, `Order`, `OrderItem`, `OrderConcurrencyIntegrationTest`
- 확인 방식:
  - 주문 상태 변경 API 경로의 service 구현을 확인했다.
  - repository lock method와 service 호출 순서를 확인했다.
  - 동시성 테스트가 요구한 실패/성공 경로를 포함하는지 확인했다.
- 결과:
  - `OrderService.updateStatus`는 `OrderRepository.findByIdForUpdate`로 대상 주문 row를 비관적 쓰기 락 조회한다.
  - `Order.requiresStockLock(targetStatus)`가 참이면 `ProductRepository.findAllByIdInOrderByIdAsc`로 상품 row 비관적 쓰기 락을 획득한다.
  - 상품 ID는 `Order.getOrderItemProductIds`에서 중복 제거 후 오름차순 정렬된다.
  - 상태 전이, 재고 검증/차감/복구, 주문 상태 변경은 `@Transactional`이 적용된 같은 service 메서드 안에서 수행된다.
  - 같은 상품 동시 완료와 같은 주문 동시 완료 테스트가 추가되었다.
- 판정: `정합`
- 잔여 리스크: 없음

### 2. 비범위 준수

- 검증 항목: 승인되지 않은 범위 확장 여부
- 대조한 입력물: `issue.md`, `plan.md`, Git diff
- 확인 방식:
  - 변경 파일 목록과 diff를 확인했다.
  - Query API, README, 예외 코드 체계, 주문 생성 재고 예약 관련 변경 여부를 확인했다.
- 결과:
  - QueryDSL 조건 조회와 주문 조회 API는 변경하지 않았다.
  - 상품 카테고리 조건 조회는 변경하지 않았다.
  - README는 변경하지 않았다.
  - 낙관적 락, 조건부 update, 주문 생성 시점 재고 예약은 도입하지 않았다.
  - 전역 예외 코드 체계는 변경하지 않았다.
- 판정: `정합`
- 잔여 리스크: 없음

### 3. 계획 대비 실제 수행 결과

- 검증 항목: `plan.md` 변경 대상과 테스트 계획 반영 여부
- 대조한 입력물: `plan.md`, Git diff, 테스트 결과
- 확인 방식:
  - 계획에 포함된 repository, domain, service, test 변경 여부를 확인했다.
  - 계획상 `OrderStockIntegrationTest`는 변경 대상으로 적었으나 실제 수정은 필요 없었고, 기존 테스트 재실행으로 순차 재고 흐름을 검증했다.
- 결과:
  - `OrderRepository`, `ProductRepository`, `Order`, `OrderItem`, `OrderService`는 계획 범위 안에서 변경되었다.
  - `OrderConcurrencyIntegrationTest`가 추가되었다.
  - `OrderServiceTest`는 `updateStatus`가 락 조회를 사용하도록 바뀐 서비스 계약에 맞춰 갱신되었다.
  - `OrderStockIntegrationTest`는 수정하지 않았지만 Phase 3/4에서 실행해 기존 순차 재고 차감/복구 흐름이 유지됨을 확인했다.
- 판정: `정합`
- 잔여 리스크: 없음

### 4. Decision 정합성

- 검증 항목: `DEC-001`, `DEC-002` 정책 준수 여부
- 대조한 입력물: `DEC-001-order-state-and-stock-policy.md`, `DEC-002-stock-concurrency-policy.md`, 구현 결과
- 확인 방식:
  - 상태 전이 정책과 재고 차감/복구 시점을 변경하지 않았는지 확인했다.
  - 비관적 락 적용 순서와 범위를 확인했다.
- 결과:
  - `DEC-001`의 상태 전이와 재고 처리 정책은 변경하지 않았다.
  - `DEC-002`의 주문 row lock 후 상품 row lock 정책을 구현했다.
  - 상품 row lock은 상품 ID 오름차순 조회 메서드로 획득한다.
  - 재고 변경이 없는 상태 전이에서는 상품 row lock을 생략해 불필요한 락 범위를 넓히지 않았다.
- 판정: `정합`
- 잔여 리스크: H2 테스트 환경에서 lock SQL과 운영 DB lock 동작은 완전히 동일하지 않을 수 있다.

## 실행하지 못한 검증

- 운영 DB 기반 동시성 검증
  - 사유: 프로젝트 기본 테스트 환경은 H2이며 Testcontainers 또는 외부 DB 사용은 필수 기준이 아니다.
  - 영향: H2에서 동시성 정합성은 검증했지만, 운영 DB별 lock wait timeout, deadlock detection, SQL dialect 차이는 별도 환경에서 추가 검증이 필요할 수 있다.
- 실제 HTTP endpoint 동시 요청 검증
  - 사유: 이번 태스크의 동시성 책임은 service transaction과 JPA repository lock 경계에 있으며, controller는 상태 변경 요청을 service로 위임하는 기존 구조를 유지한다.
  - 영향: MockMvc/HTTP 레벨 동시 요청은 별도 end-to-end 성격 검증으로 남길 수 있다.

## 결과 요약

- 주문 상태 변경 경로에 주문 row 비관적 쓰기 락을 적용했다.
- 재고 변경이 필요한 상태 전이에서 상품 row 비관적 쓰기 락을 적용했다.
- 주문 row lock 이후 상품 row lock 순서를 유지했다.
- 상품 ID 오름차순으로 상품 row lock을 획득한다.
- 같은 상품 동시 완료 요청과 같은 주문 동시 완료 요청의 핵심 failure path를 통합 테스트로 검증했다.
- 기존 순차 재고 차감/복구와 전체 테스트가 통과했다.
- Phase 1의 요청사항, 제약사항, 비범위 대비 `누락` 또는 `범위 확장`은 확인되지 않았다.

## Phase 5에서 반영할 related decisions/

- 새 decision 생성: 필요 없음
  - 사유: 이번 태스크는 기존 `DEC-002`의 비관적 락 정책을 구현한 작업이며 동시성 전략 자체를 변경하지 않았다.
- 수정 후보:
  - `docs/project/decisions/DEC-002-stock-concurrency-policy.md`
    - 현재 구현 결과와 정책이 일치하므로 필수 수정은 없다.
  - `README.md`
    - 최종 제출 문서에서 비관적 락 구현 방식과 H2 기반 검증 범위를 실제 구현 결과에 맞춰 설명할 필요가 있다.
    - 다만 README 최종 정리는 `docs/final-readme` 비범위로 유지한다.

## 남은 리스크

- H2 기반 동시성 테스트는 운영 DB의 모든 락 동작을 대체하지 않는다.
- HTTP endpoint 레벨의 동시 요청 검증은 수행하지 않았다.
- 현재 예외 코드는 재고 부족과 상태 전이 오류를 더 세분화된 API code로 분리하지 않는다. 이는 이번 태스크 비범위다.

## 후속 조치 필요 사항

- `docs/final-readme`에서 동시성 제어 구현 방식을 실제 코드 기준으로 정리한다.
- 운영 DB를 별도로 지정하는 환경이 생기면 동일 동시성 시나리오를 해당 DB에서 재검증한다.
- 필요 시 후속 태스크에서 도메인별 예외 코드를 세분화한다.

## Phase 5 Documentation

- 새 decision 생성: 없음
- 기존 decision 수정: 없음
  - `docs/project/decisions/DEC-002-stock-concurrency-policy.md`는 현재 구현 결과와 일치한다.
- README 수정: 없음
  - 최종 제출 README 정리는 `docs/final-readme` 비범위로 유지한다.
- close-out 문서 위치:
  - `docs/task/007_order-concurrency-control/issue.md`
  - `docs/task/007_order-concurrency-control/requirements.md`
  - `docs/task/007_order-concurrency-control/plan.md`
  - `docs/task/007_order-concurrency-control/implementation_notes.md`
  - `docs/task/007_order-concurrency-control/validation_report.md`
  - `docs/task/007_order-concurrency-control/phase_status.md`
- 최종 검증 기준:
  - `./mvnw test`
  - `python3 scripts/validate_phase_gate.py docs/task/007_order-concurrency-control --git-scope repo`
  - `git diff --check`
- 후속 작업:
  - `docs/final-readme`에서 비관적 락 구현 방식과 H2 기반 검증 범위를 최종 README에 반영한다.

## Phase 4 감사 결과

- 자동 검증과 수동 검증이 구분되어 있다.
- 실행한 검증의 명령, 결과, 판정이 기록되어 있다.
- 미실행 검증과 사유가 기록되어 있다.
- `issue.md`, `requirements.md`, `plan.md`, `implementation_notes.md`, 실제 구현 결과를 교차 검토했다.
- Phase 1 요청사항, 제약사항, 비범위 대비 누락 또는 범위 확장은 확인되지 않았다.
- related decisions 반영 필요 여부를 검토했다.
- 잔여 리스크와 후속 조치를 기록했다.
- 판정: 승인 가능

## Phase 5 감사 결과

- Phase 4가 넘긴 related decisions 판단을 확인했다.
- 새 decision 또는 기존 decision 수정이 필요 없다는 판단은 최신 구현 결과와 `DEC-002` 대조 기준으로 유지된다.
- README 최종 정리는 이번 태스크 비범위로 남겼다.
- 작업 로그가 변경 목적, 구현 결과, 검증 결과, 후속 조치를 복원 가능하게 남긴다.
- 범위 밖 개선 사항은 후속 후보로 분리되었다.
- 판정: 승인 가능
