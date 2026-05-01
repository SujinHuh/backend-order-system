# Quality Gate Profile

## 목적

이 문서는 주문 시스템 API 서버에서 실제로 실행할 품질 게이트와 실패 기준을 정의한다.

테스트 환경, 통합 테스트 범위, coverage 기준 같은 세부 테스트 기준은 `docs/project/standards/testing_profile.md`에서 정의한다.

## Formatter

- 명령: 별도 formatter 플러그인 도입 전까지 수동 적용
- 필수 여부: 선택
- 적용 시점: Phase 2 구현 중 파일 수정 시
- 실패 처리 기준: import 정렬, 들여쓰기, 불필요한 공백이 명확히 깨진 경우 수정한다.

## Linter

- 명령: 별도 linter 플러그인 도입 전까지 수동 검토
- 필수 여부: 선택
- 적용 시점: Phase 2 감사 전
- 실패 처리 기준: 미사용 import, dead code, 임시 로그, 주석 처리된 코드는 제거한다.

## Type Checker / Compile

- 명령: `./mvnw test`
- 필수 여부: 필수
- 적용 시점: Phase 2 종료 전, Phase 3 종료 전, Phase 4 검증 시
- 실패 처리 기준: 컴파일 실패가 있으면 Phase 종료 불가

## Test

- 명령: `./mvnw test`
- 필수 여부: 필수
- 적용 시점: 구현 단위 완료 시, Phase 3 종료 전, 제출 전
- 실패 처리 기준: 실패 테스트가 있으면 해당 실패 원인을 수정하거나 미실행/실패 사유를 `validation_report.md`에 명시한다.
- 세부 테스트 범위: `docs/project/standards/testing_profile.md`를 따른다.

## Architecture Rule

- 명령: 자동화 도구 없음
- 필수 여부: 수동 검토 필수
- 적용 시점: Phase 2 감사 전
- 실패 처리 기준:
  - Controller가 repository를 직접 호출하면 수정한다.
  - Domain이 controller DTO 또는 HTTP 타입에 의존하면 수정한다.
  - 재고 변경이 트랜잭션 밖에서 수행되면 수정한다.
  - 주문 상태 전이 규칙이 domain 또는 service에 명시되지 않으면 수정한다.

## Performance Check

- 명령: 자동 benchmark 없음
- 필수 여부: 수동 검토 필수
- 적용 시점: Phase 4 검증 시
- 실패 처리 기준:
  - 목록 조회에 페이징이 없으면 수정한다.
  - 주문 목록의 상태/기간 조건이 명확한 조회 경로로 구현되지 않으면 수정한다.
  - N+1 가능성이 확인되면 fetch join, EntityGraph, DTO projection 중 하나를 적용하거나 README에 개선 방안을 남긴다.

## 제출 전 최소 게이트

제출 전 다음 명령은 실행 대상이다.

```bash
./mvnw test
```

명령을 실행하지 못한 경우 사유를 README 또는 `validation_report.md`에 기록한다.
