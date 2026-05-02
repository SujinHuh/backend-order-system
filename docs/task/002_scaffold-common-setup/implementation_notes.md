# Implementation Notes

## 진행 로그

### 2026-05-02

- `feat/scaffold-common-setup` 브랜치에서 기능별 첫 task workspace를 생성했다.
- 상위 마스터 task인 `001_order-system-api`와 분리해 스캐폴딩 및 공통 설정만 이번 범위로 제한했다.
- 기능별 task 구조 전환을 위해 상위 마스터 task의 `plan.md`, `implementation_notes.md`와 프로젝트 `implementation_order.md`도 함께 정리했으므로, `phase_status.md`의 allowed write-set에 해당 경로를 명시했다.
- 독립 감사 결과를 반영해 allowed write-set을 필요한 project standard 파일로 좁히고, Q-class 검증용 entity는 테스트 전용 타입으로 제한했다.
- `validation_report.md`는 task 공통 산출물 placeholder로 생성했으며, 실제 검증 기록은 Phase 2 이후 최신 실행 결과로 갱신한다.
- Phase 2를 시작했다. 로컬 Java는 21.0.7로 Java 17 이상 조건을 만족하지만, `mvn` 명령은 설치되어 있지 않아 `mvnw` 실행 경로를 함께 구성해야 한다.
- Spring Boot 3.3.5 / Java 17 target / Maven Wrapper 3.9.9 기반으로 `pom.xml`을 구성했다.
- Spring Web, Spring Data JPA, Validation, H2, Test, QueryDSL JPA 5.1.0 Jakarta 의존성과 QueryDSL annotation processor를 추가했다.
- `OrderSystemApplication`을 생성하고 `@EnableJpaAuditing`을 활성화했다.
- H2 in-memory DB, H2 console, JPA `create-drop`, `open-in-view: false` 기본 설정을 `application.yml`에 추가했다.
- Spring context load 테스트와 QueryDSL Q-class 생성 확인용 테스트 전용 entity를 추가했다.
- Maven Wrapper는 공식 wrapper plugin으로 생성했으며, 현재 설정은 `distributionType=only-script`, Maven `3.9.9` 다운로드 방식이다.
- `./mvnw test-compile`로 `target/generated-test-sources/test-annotations/com/example/ordersystem/support/QQuerydslProbeEntity.java` 생성을 확인했다.
- `./mvnw test`로 Spring Boot context load, JPA, H2 설정이 함께 동작하는지 확인했다.
- Phase 2 감사 결과, 이번 변경은 스캐폴딩/빌드/최소 검증 경로에 제한되어 있고 상품/주문 도메인, API, 공통 예외 처리, QueryDSL custom repository는 후속 task 범위로 남아 있다.
- Phase 3로 이동해 통합 책임을 검토했다. 이번 task의 통합 대상은 bootstrap, Spring context wiring, JPA/H2 연결, QueryDSL annotation processing이며, 별도 비즈니스 통합 테스트 대상은 아직 없다.
- Phase 3 품질 게이트로 `./mvnw test`를 다시 실행해 Spring Boot context, JPA/Hibernate, H2 datasource 연결이 실제 조립 상태에서 통과함을 확인했다.
- Phase 3 감사 결과, Phase 2에서 위임된 책임 중 이번 task에서 즉시 검증할 항목은 모두 확인되었고, 실제 상품/주문 도메인 Q-class 및 repository/query 통합 검증은 후속 도메인/query task로 유지한다.
- Phase 3 감사 판정은 승인 가능이다. 승인 불가 기준인 위임 책임 누락, stale 감사 재사용, 내부 협력 mock 대체, 연결 책임 없는 형식적 통합 테스트, 과도한 검증 범위 확장은 확인되지 않았다.
- Phase 4로 이동해 `issue.md`, `requirements.md`, `plan.md`, `implementation_notes.md`, 실제 변경 파일을 교차 검토했다.
- Phase 4 최종 게이트로 `./mvnw test`, `./mvnw test-compile`, phase gate, overlay consistency, overlay decision readiness `phase2`를 재실행했다.
- Phase 4 추적성 검토 결과, 요청사항과 성공 기준은 충족했고 비범위인 도메인/API/예외 처리/동시성/최종 README 구현은 포함하지 않았다.
- Phase 4 감사 판정은 승인 가능이다. 자동/수동 검증 구분, 미실행 검증 사유, 추적성 검토, related decisions 판단, 잔여 리스크 기록이 모두 확인되었다.
- 독립 서브에이전트 Phase 4 감사에서 제한된 실행 조건의 `./mvnw test`가 Mockito/ByteBuddy self-attach 실패를 낼 수 있다는 지적을 받았다.
- 테스트 JVM 환경 차이를 줄이기 위해 Mockito mock maker를 test resource에 명시하고, Surefire `argLine`에 `-Djdk.attach.allowAttachSelf=true -XX:+EnableDynamicAgentLoading`을 추가했다.
- 감사 지적 반영 후 `./mvnw test`, `./mvnw test-compile`, phase gate, overlay consistency, overlay decision readiness `phase2`, `git diff --check`를 다시 실행했고 모두 통과했다.
- Phase 5 close-out을 수행했다. Phase 4의 related decisions 판단은 `해당 없음`이므로 `docs/project/decisions/*`는 수정하지 않았다.
- 최종 작업 결과는 Maven/Spring Boot 스캐폴딩, H2/JPA 설정, QueryDSL annotation processing 검증 경로, 테스트 JVM 안정화 설정, task-local 검증 기록이다.
- 범위 밖 항목인 상품/주문 도메인, API, 공통 예외 처리, 동시성 제어, QueryDSL custom repository, 최종 README 정리는 후속 task로 유지한다.
- Phase 5 감사 결과, 필요한 문서 반영과 작업 로그가 task-local 문서에 남아 있으며 stale 상태의 Validation 결과를 재사용하지 않았다.

## 경량 검토 기록

- 작은 태스크로 본 근거: 스캐폴딩과 빌드 설정 중심이며 비즈니스 API 구현이 없다.
- 경량 적용 승인 여부: 아직 미정.
- 실제 축소한 범위: 없음.
- 유지한 테스트: `./mvnw test`, 필요 시 compile/test-compile.
- 유지한 감사: Phase 문서와 phase gate 검증.
- 전체 흐름 영향 요약: 이후 모든 기능 이슈의 빌드/실행 기반을 제공한다.
- 남은 리스크: QueryDSL custom repository와 실제 도메인 Q-class 생성은 후속 task에서 도메인 entity가 생긴 뒤 다시 검증해야 한다.
- Full 전환 조건 또는 승격 조건: 도메인 entity나 API 구현이 이번 task에 포함되면 Full 범위로 전환한다.

## 구현 중 결정 사항

- repo-local 근거: `docs/project/standards/implementation_order.md`, `docs/task/001_order-system-api/plan.md`
- repo에 없어 문서화/승인 대상으로 넘긴 결정: 없음.

## 위임된 책임

- 실제 QueryDSL custom repository 구현은 `docs/task/008_query-apis`에서 수행한다.
- QueryDSL Q-class 생성 확인에 테스트 전용 entity를 사용하더라도 상품/주문 도메인 entity 구현 책임은 후속 도메인 task로 유지한다.
- 실제 상품/주문 도메인의 JPA mapping, repository, QueryDSL 조건 조회 통합 테스트는 해당 도메인/entity/query task에서 검증한다.
- Mockito/ByteBuddy 테스트 JVM 옵션은 이번 스캐폴딩의 테스트 실행 안정화를 위한 설정이며, 실제 mocking 기반 테스트 정책은 후속 기능 테스트에서 별도 판단한다.

## 사용자 승인 필요 항목

- 없음.

## 후속 태스크 후보

- `003_global-exception-handler`
- `004_product-api`
- `008_query-apis`
