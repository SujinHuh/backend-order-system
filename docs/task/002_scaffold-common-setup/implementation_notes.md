# Implementation Notes

## 진행 로그

### 2026-05-02

- `feat/scaffold-common-setup` 브랜치에서 기능별 첫 task workspace를 생성했다.
- 상위 마스터 task인 `001_order-system-api`와 분리해 스캐폴딩 및 공통 설정만 이번 범위로 제한했다.
- 기능별 task 구조 전환을 위해 상위 마스터 task의 `plan.md`, `implementation_notes.md`와 프로젝트 `implementation_order.md`도 함께 정리했으므로, `phase_status.md`의 allowed write-set에 해당 경로를 명시했다.
- 독립 감사 결과를 반영해 allowed write-set을 필요한 project standard 파일로 좁히고, Q-class 검증용 entity는 테스트 전용 타입으로 제한했다.
- `validation_report.md`는 task 공통 산출물 placeholder로 생성했으며, 실제 검증 기록은 Phase 2 이후 최신 실행 결과로 갱신한다.

## 경량 검토 기록

- 작은 태스크로 본 근거: 스캐폴딩과 빌드 설정 중심이며 비즈니스 API 구현이 없다.
- 경량 적용 승인 여부: 아직 미정.
- 실제 축소한 범위: 없음.
- 유지한 테스트: `./mvnw test`, 필요 시 compile/test-compile.
- 유지한 감사: Phase 문서와 phase gate 검증.
- 전체 흐름 영향 요약: 이후 모든 기능 이슈의 빌드/실행 기반을 제공한다.
- 남은 리스크: QueryDSL annotation processor 설정과 Maven wrapper 구성.
- Full 전환 조건 또는 승격 조건: 도메인 entity나 API 구현이 이번 task에 포함되면 Full 범위로 전환한다.

## 구현 중 결정 사항

- repo-local 근거: `docs/project/standards/implementation_order.md`, `docs/task/001_order-system-api/plan.md`
- repo에 없어 문서화/승인 대상으로 넘긴 결정: 없음.

## 위임된 책임

- 실제 QueryDSL custom repository 구현은 `docs/task/008_query-apis`에서 수행한다.
- QueryDSL Q-class 생성 확인에 테스트 전용 entity를 사용하더라도 상품/주문 도메인 entity 구현 책임은 후속 도메인 task로 유지한다.

## 사용자 승인 필요 항목

- 없음.

## 후속 태스크 후보

- `003_global-exception-handler`
- `004_product-api`
