# Plan

## 변경 대상 파일 또는 모듈

- `README.md`
- `docs/task/final-readme/issue.md`
- `docs/task/final-readme/requirements.md`
- `docs/task/final-readme/plan.md`
- `docs/task/final-readme/implementation_notes.md`
- `docs/task/final-readme/phase_status.md`
- `docs/task/final-readme/validation_report.md`

## 레이어별 작업 계획

### 1. 문서 근거 수집

- 현재 `README.md`를 실제 구현과 대조한다.
- `docs/project/standards/architecture.md`, `implementation_order.md`, `testing_profile.md`, `quality_gate_profile.md`를 확인한다.
- `docs/project/decisions/README.md`와 관련 decision 문서를 확인한다.
- `docs/task/001_order-system-api` ~ `docs/task/008_query-apis`의 requirements와 validation 결과를 확인한다.
- controller/service/domain/repository/test 파일을 필요한 범위에서 확인해 README 문구의 근거로 사용한다.

### 2. README 구조 정리

- README를 최종 제출 문서 구조로 재정리한다.
- 프로젝트 개요, 기술 스택, 실행 방법, 테스트 방법을 명확히 작성한다.
- 도메인 모델, 상태 전이, 재고 정책, 동시성 정책, 조회 전략을 실제 구현 기준으로 작성한다.
- 상품/주문 API 목록과 주요 요청 예시를 작성한다.
- 예외 응답과 대표 예외 상황을 실제 공통 에러 흐름 기준으로 작성한다.
- 테스트/검증 범위와 남은 고려사항을 과장 없이 작성한다.

### 3. 검증

- `./mvnw test`를 실행해 문서 변경 중 기능 회귀가 없음을 확인한다.
- `git diff --check`를 실행한다.
- `python3 scripts/validate_phase_gate.py docs/task/final-readme --git-scope repo`를 실행한다.
- README의 API/정책 문구를 controller/service/domain/repository 구현과 수동 대조한다.

## 테스트 계획

- 전체 테스트:
  - `./mvnw test`
- 문서/phase 검증:
  - `git diff --check`
  - `python3 scripts/validate_phase_gate.py docs/task/final-readme --git-scope repo`
- 수동 traceability 검증:
  - README의 endpoint 목록과 controller 매핑 대조
  - README의 상태 전이/재고 정책과 domain/service 테스트 대조
  - README의 QueryDSL 조회 설명과 repository/custom test 대조
  - README의 비범위/잔여 고려사항과 validation report 대조

## 문서 반영 계획

- `README.md`를 최종 제출 문서로 갱신한다.
- `implementation_notes.md`에 Phase별 진행 로그와 근거 문서를 기록한다.
- `validation_report.md`에 실행 검증, 미실행 검증, 남은 리스크, Phase 5 related decisions 판단을 기록한다.
- 새 `docs/project/decisions/*` 문서 생성 또는 수정은 계획하지 않는다.
- README 작성 중 새 장기 정책이 필요해지면 Phase 상태를 stale 처리하고 decision 반영 계획을 다시 수립한다.

## 비범위

- 기능 코드 변경
- 테스트 코드 변경
- API 계약 변경
- 예외 코드 세분화
- DB migration/Testcontainers/운영 DB 설정 추가
- 새로운 architecture 또는 decision 정책 확정

## 리스크 또는 확인 포인트

- README가 구현 전 설계 문구를 유지하면 실제 구현과 혼동될 수 있다.
- `PageImpl` 직렬화 warning, query count 자동 검증 부재, DB index/migration 미도입 같은 잔여 리스크는 완료된 기능처럼 표현하지 않고 고려사항으로 분리해야 한다.
- 최종 제출 문서가 너무 내부 process 문서 중심이 되면 사용자가 실행/검증 방법을 빠르게 파악하기 어려울 수 있다.

## Phase 1 내부 감사 결과

### Requirements Audit

- `issue.md`의 최종 README 현행화 요청, 실제 구현 기준 문서화, 비범위가 `requirements.md`에 반영되었다.
- API, 정책, 예외, 실행/검증, decision 반영 후보가 구현 가능한 수준으로 구체화되었다.
- 새 정책 확정이 필요한 항목은 현재 없고, `docs/project/decisions/*` 수정은 계획하지 않는 판단이 남아 있다.
- 판정: 승인 가능

### Plan Audit

- `plan.md`는 README 갱신, 근거 수집, 검증, 문서 반영 계획을 포함한다.
- 기능/테스트 코드 변경과 정책 변경은 비범위로 분리되었다.
- decision 반영이 불필요하다는 현재 판단과, 새 정책 필요 시 stale 처리 후 재수행한다는 조건이 포함되었다.
- 판정: 승인 가능

### Issue 대비 Plan 누락 감사

- `issue.md`의 요청사항은 README 구조 정리, 근거 수집, 검증 계획에 반영되었다.
- `issue.md`의 비범위와 제약 조건은 `plan.md`의 비범위, 문서 반영 계획, 리스크에 반영되었다.
- 판정: 승인 가능
