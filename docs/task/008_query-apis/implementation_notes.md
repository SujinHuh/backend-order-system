# Implementation Notes

## 진행 로그

- Phase 1 시작:
  - `main` 최신 상태에서 `feat/query-apis` 브랜치를 생성하고 체크아웃했다.
  - `docs/entrypoint.md`, `docs/process/harness_guide.md`, 프로젝트 architecture, implementation order, testing profile, quality gate profile을 확인했다.
  - README, `docs/task/001_order-system-api`, `docs/task/004_product-api`, `docs/task/005_order-create-api`, `docs/task/006_order-status-stock`, `docs/task/007_order-concurrency-control`에서 조회 요구사항과 비범위를 확인했다.
  - 현재 `ProductController`, `ProductService`, `ProductRepository`, `OrderController`, `OrderService`, `OrderRepository`, `OrderResponse` 구현 상태를 확인했다.
- Phase 1 산출물 작성:
  - `issue.md`에 상품/주문 조회 API의 배경, 요청사항, 비범위, 제약 조건을 정리했다.
  - `requirements.md`에 상품 카테고리 조건 조회, 주문 단건 조회, 주문 목록 상태/기간 조건 조회, QueryDSL custom repository, N+1 방지 요구사항을 정의했다.
  - `plan.md`에 repository -> service -> controller -> test 순서의 작업 계획과 테스트/문서 반영 계획을 작성했다.
- Phase 1 내부 감사:
  - requirements audit: 승인 가능
  - plan audit: 승인 가능
  - issue 대비 plan 누락 감사: 승인 가능
  - 현재 상태는 Phase 1 사용자 승인 대기다.
- Phase 1 감사 follow-up:
  - subagent 감사에서 repo-local 근거 없이 `from > to`를 400 오류 계약으로 확정한 점이 지적되었다.
  - 최신 `main`(`7a942a4`) 기준으로 002~007 audit follow-up 병합 상태를 반영했다.
  - `from > to` 별도 오류 계약은 제거하고, `from`, `to`는 명세된 inclusive predicate 조합으로만 정의했다.
  - `docs/task/007_order-concurrency-control/*`를 repo-local 근거 목록에 추가했다.
- Phase 1 검증:
  - `docs/process/phases/phase_1_requirement_and_planning/audit.md`, `docs/process/phases/phase_1_requirement_and_planning/implementation.md`, `docs/process/common/audit_policy.md`, `docs/process/common/artifact_policy.md`를 재확인했다.
  - `issue.md`, `requirements.md`, `plan.md`, `phase_status.md`를 최신본 기준으로 대조했다.
  - requirements audit, plan audit, issue 대비 plan 누락 감사 모두 승인 가능으로 재판정했다.
  - `python3 scripts/validate_phase_gate.py docs/task/008_query-apis` 통과를 확인했다.
  - 현재 상태는 Phase 1 사용자 승인 대기다.
- Phase 1 subagent 감사 follow-up:
  - subagent 감사에서 주문 목록 `Page<OrderResponse>` 매핑 경로의 N+1 방지 전략과 검증 계획이 `plan.md`에 명시되지 않은 점이 지적되었다.
  - `plan.md`의 repository/service/test 계획에 주문 목록 조회 fetch 전략과 주문 항목/상품 응답 검증을 추가했다.
  - `plan.md` 수정으로 이전 Phase 1 감사 결과는 stale 처리하고 최신본 기준 재감사가 필요하다.
- Phase 1 재감사 및 Phase 2 진입:
  - subagent 재감사 결과 Phase 1 승인 가능 판정을 받았다.
  - 사용자 `phase 2 진행` 승인에 따라 `phase_status.md`를 Phase 2 TDD Implementation으로 전환했다.
  - Phase 2는 repository -> service -> controller 순서로 실패 테스트 작성 후 최소 구현을 진행한다.
- Phase 2 Repository:
  - `ProductRepositoryQueryTest`, `OrderRepositoryQueryTest`를 먼저 추가해 custom repository 계약 부재로 실패하는 상태를 확인했다.
  - `ProductRepositoryCustom`/`ProductRepositoryImpl`, `OrderRepositoryCustom`/`OrderRepositoryImpl`을 추가하고 기존 repository가 custom interface를 확장하도록 구현했다.
  - `./mvnw test -Dtest=ProductRepositoryQueryTest,OrderRepositoryQueryTest` 통과를 확인했다.
- Phase 2 Service:
  - `ProductServiceTest`, `OrderServiceTest`에 카테고리 상품 조회, 주문 단건 조회, 주문 목록 조건 조회 테스트를 먼저 추가했다.
  - 서비스 테스트에서 새 메서드 시그니처 부재로 실패하는 상태를 확인한 뒤 service 구현을 반영했다.
  - `./mvnw test -Dtest=ProductServiceTest,OrderServiceTest` 통과를 확인했다.
- Phase 2 Controller:
  - `ProductControllerTest`, `OrderControllerTest`에 `category`, 주문 단건 조회, 주문 목록 조건 조회 요청/응답 테스트를 먼저 추가했다.
  - controller 테스트에서 endpoint/parameter 연결 부재로 실패하는 상태를 확인한 뒤 controller 구현을 반영했다.
  - `./mvnw test -Dtest=ProductControllerTest,OrderControllerTest` 통과를 확인했다.
- Phase 2 전체 검증:
  - 주문 목록 조회의 페이지 ID 조회 후 fetch join 조회 결과를 원래 페이지 순서로 복원하는 경로에서 `indexOf` 기반 O(n²) 매핑을 O(n) index map으로 정리했다.
  - `./mvnw test` 통과를 확인했다. 결과: Tests run: 62, Failures: 0, Errors: 0, Skipped: 0.
  - `git diff --check` 통과를 확인했다.
  - `python3 scripts/validate_phase_gate.py docs/task/008_query-apis --git-scope repo` 통과를 확인했다.
- Phase 2 subagent 감사 follow-up:
  - subagent 감사에서 주문 목록 out-of-range page의 `totalElements`가 0으로 떨어질 수 있는 count 계약 결함이 지적되었다.
  - controller 테스트가 query parameter를 service 인자로 전달하는 계약을 명시적으로 검증하지 않는다는 테스트 부족이 지적되었다.
  - Phase 3 진입은 보류하고 Phase 2 follow-up 후 재감사를 진행한다.
- Phase 2 follow-up 반영:
  - `OrderRepositoryQueryTest`와 `ProductRepositoryQueryTest`에 out-of-range page에서도 `totalElements`가 유지되는 테스트를 추가했다.
  - `OrderRepositoryImpl`과 `ProductRepositoryImpl`이 명시적인 count query 결과로 `PageImpl`을 생성하도록 수정했다.
  - `ProductControllerTest`, `OrderControllerTest`에 query parameter가 service 인자로 전달되는지 `ArgumentCaptor` 검증을 추가했다.
  - 감사 지적 재현: `./mvnw test -Dtest=ProductRepositoryQueryTest,OrderRepositoryQueryTest,ProductControllerTest,OrderControllerTest`에서 주문 목록 out-of-range count 실패를 확인했다.
  - follow-up 후 같은 targeted test 통과를 확인했다. 결과: Tests run: 26, Failures: 0, Errors: 0, Skipped: 0.
  - `./mvnw test` 통과를 확인했다. 결과: Tests run: 64, Failures: 0, Errors: 0, Skipped: 0.
  - `git diff --check`와 `python3 scripts/validate_phase_gate.py docs/task/008_query-apis --git-scope repo` 통과를 확인했다.
- Phase 2 재감사 및 Phase 3 진입:
  - subagent 재감사 결과 Phase 2 승인 가능 판정을 받았다.
  - 사용자 `phase3 시작` 승인에 따라 `phase_status.md`를 Phase 3 Integration으로 전환했다.
  - Phase 3에서는 실제 controller-service-repository-DB 연결 책임을 MockMvc 통합 테스트로 검증한다.
- Phase 3 Integration:
  - `ProductQueryApiIntegrationTest`를 추가해 `GET /api/products?category=FOOD`가 실제 controller-service-repository-DB 경로에서 카테고리 조건을 적용하는지 검증했다.
  - `OrderQueryApiIntegrationTest`를 추가해 `GET /api/orders/{id}`가 실제 DB 주문의 주문 항목/상품 정보를 반환하는지 검증했다.
  - `OrderQueryApiIntegrationTest`를 추가해 `GET /api/orders?status=PENDING`가 실제 controller-service-repository-DB 경로에서 상태 조건과 페이징을 적용하는지 검증했다.
  - `./mvnw test -Dtest=ProductQueryApiIntegrationTest,OrderQueryApiIntegrationTest` 통과를 확인했다. 결과: Tests run: 3, Failures: 0, Errors: 0, Skipped: 0.
  - `./mvnw test` 통과를 확인했다. 결과: Tests run: 67, Failures: 0, Errors: 0, Skipped: 0.
  - `git diff --check`와 `python3 scripts/validate_phase_gate.py docs/task/008_query-apis --git-scope repo` 통과를 확인했다.
- Phase 3 subagent 감사 follow-up:
  - subagent 감사에서 JPA fetch 전략 위임 책임이 lazy loading으로도 통과 가능한 방식으로만 검증된 점이 지적되었다.
  - 실제 HTTP query binding부터 DB 조회까지 이어지는 `from`, `to`, `status+from+to` 조합 통합 테스트가 부족하다는 점이 지적되었다.
  - 실제 연결 책임을 포함한 의미 있는 failure/edge path 통합 테스트가 부족하다는 점이 지적되었다.
  - Phase 4 진입은 보류하고 Phase 3 follow-up 후 재감사를 진행한다.
- Phase 3 follow-up 반영:
  - `OrderRepositoryQueryTest`에서 Hibernate 초기화 상태를 검증해 주문 상세/목록 조회의 `orderItems`와 `product` fetch 전략이 깨지면 실패하도록 보강했다.
  - `OrderQueryApiIntegrationTest`에 `status+from+to` 조합 조건이 실제 HTTP query binding부터 DB 조회까지 적용되는 통합 테스트를 추가했다.
  - `OrderQueryApiIntegrationTest`에 존재하지 않는 주문 단건 조회 404 응답과 잘못된 date-time query parameter 400 응답 통합 테스트를 추가했다.
  - `./mvnw test -Dtest=OrderRepositoryQueryTest,OrderQueryApiIntegrationTest,ProductQueryApiIntegrationTest` 통과를 확인했다. 결과: Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.
  - `./mvnw test` 통과를 확인했다. 결과: Tests run: 70, Failures: 0, Errors: 0, Skipped: 0.
  - `git diff --check`와 `python3 scripts/validate_phase_gate.py docs/task/008_query-apis --git-scope repo` 통과를 확인했다.
- Phase 3 재감사 및 Phase 4 진입:
  - subagent 재감사 결과 Phase 3 승인 가능 판정을 받았다.
  - 사용자 `phase4 진행` 승인에 따라 `phase_status.md`를 Phase 4 Validation으로 전환했다.
  - Phase 4에서는 자동 검증, 산출물-구현 traceability, 미실행 검증 사유, residual risk를 `validation_report.md`에 기록한다.
- Phase 4 Validation:
  - `./mvnw test` 통과를 확인했다. 결과: Tests run: 70, Failures: 0, Errors: 0, Skipped: 0.
  - `git diff --check` 통과를 확인했다.
  - `python3 scripts/validate_phase_gate.py docs/task/008_query-apis --git-scope repo` 통과를 확인했다.
  - `rg -n "TODO|FIXME|System\.out|printStackTrace" src/main src/test docs/task/008_query-apis`에서 매치가 없음을 확인했다.
  - `validation_report.md`에 자동 검증, 수동 traceability 검증, 미실행 검증 사유, Phase 5 related decisions 판단, 잔여 리스크를 기록했다.
- Phase 4 subagent 감사:
  - subagent 감사 결과 Phase 4 승인 가능 판정을 받았다.
  - `validation_report.md`가 자동/수동 검증, 미실행 검증 사유, traceability, related decisions 판단, residual risk를 모두 기록했다고 확인했다.
  - Phase 5 진입 전 blocking issue는 없다고 판정했다.
  - 비차단 개선점으로 문서까지 포함한 `rg` 재검색 시 기록된 명령 문자열 자체가 매칭될 수 있으므로, Phase 5에서 필요하면 `src/main src/test` 범위로 좁혀 재기록할 수 있다고 확인했다.
- Phase 5 Documentation/Close-out:
  - 사용자 `phase 5 시작` 승인에 따라 `phase_status.md`를 Phase 5 Documentation/Close-out으로 전환했다.
  - Phase 4의 `Phase 5에서 반영할 related decisions/`가 `해당 없음`임을 재확인했고, `docs/project/decisions/*` 수정은 수행하지 않았다.
  - Phase 4 감사의 비차단 개선점을 반영해 코드 범위 기준 `rg -n "TODO|FIXME|System\.out|printStackTrace" src/main src/test`로 재검증했고 매치가 없음을 확인했다.
  - `./mvnw test`를 재실행해 통과를 확인했다. 결과: Tests run: 70, Failures: 0, Errors: 0, Skipped: 0.
  - `validation_report.md`의 후속 조치 필요 사항에 Phase 5 close-out 결과를 반영했다.
  - `git diff --check`와 `python3 scripts/validate_phase_gate.py docs/task/008_query-apis --git-scope repo` 통과를 확인했다.
- Phase 5 내부 감사:
  - 최신 `validation_report.md` 기준으로 related decisions 반영 누락이 없음을 확인했다.
  - 작업 로그가 변경 배경, 구현 결과, 검증 결과, 후속 후보를 복원 가능하게 남긴다고 판정했다.
  - 범위 밖 개선 사항은 `후속 태스크 후보`와 `validation_report.md` 잔여 리스크로 분리되어 있음을 확인했다.
  - 판정: 승인 가능

## 경량 검토 기록

- 작은 태스크로 본 근거: 해당 없음. 조회 API와 QueryDSL custom repository는 여러 레이어와 테스트에 걸친 기능 작업이므로 full Phase 절차를 따른다.
- 경량 적용 승인 여부: 해당 없음
- 실제 축소한 범위: 해당 없음
- 유지한 테스트: Phase 2/3에서 repository/service/controller 테스트와 `./mvnw test` 유지 예정
- 유지한 감사: Phase 1 내부 감사 3종 유지
- 전체 흐름 영향 요약: 상품/주문 조회 API와 repository 조회 전략을 추가하므로 full 검증 필요
- 남은 리스크: QueryDSL 페이징 count query와 주문 응답 N+1 방지 전략 검증 필요
- Full 전환 조건 또는 승격 조건: 이미 full 절차 적용

## 구현 중 결정 사항

- repo-local 근거:
  - `README.md`
  - `docs/project/standards/architecture.md`
  - `docs/project/standards/implementation_order.md`
  - `docs/project/standards/testing_profile.md`
  - `docs/task/001_order-system-api/requirements.md`
  - `docs/task/001_order-system-api/plan.md`
  - `docs/task/004_product-api/*`
  - `docs/task/005_order-create-api/*`
  - `docs/task/006_order-status-stock/*`
  - `docs/task/007_order-concurrency-control/*`
- repo에 없어 문서화/승인 대상으로 넘긴 결정:
  - `from > to` 별도 400 오류 정책은 repo-local 근거가 없어 이번 태스크에서 API 계약으로 추가하지 않는다.

## 위임된 책임

- QueryDSL query correctness와 JPA fetch 전략은 단위 테스트만으로 충분하지 않으므로 Phase 3 통합 테스트 검증 대상으로 유지한다.

## 사용자 승인 필요 항목

- Phase 5 Documentation/Close-out 감사 후 최종 사용자 승인이 필요하다.

## 후속 태스크 후보

- `docs/final-readme`: 최종 README와 실제 구현 범위 정합성 정리
- 예외 코드 세분화: enum/date-time binding, invalid search condition, not found 등 API 에러 코드 구체화
