# Commit Rule

## 목적

이 문서는 주문 시스템 API 서버 작업에서 커밋을 만들 때 지킬 기준을 정의한다.

## 기본 원칙

- 컴파일 실패 상태로 커밋하지 않는다.
- 테스트 실패를 알고 있는 상태로 커밋하지 않는다.
- 하나의 커밋은 하나의 의도를 설명할 수 있어야 한다.
- 현재 과제와 무관한 대규모 리팩터링은 분리한다.
- 임시 로그, 주석 처리된 코드, 사용하지 않는 import는 커밋하지 않는다.

## 커밋 전 최소 점검 항목

제출 전 최소 확인 명령은 다음과 같다.

```bash
./mvnw test
```

프로젝트 스캐폴딩 전처럼 명령 실행이 불가능한 시점에는 실행 불가 사유를 작업 로그에 남긴다.

- compile 오류가 없는지 확인한다.
- type 오류가 없는지 확인한다.
- build 실패가 없는지 확인한다.
- test 또는 테스트 실패가 없는지 확인한다.

## 메시지 형식

Conventional Commit 형식을 권장한다.

```text
<type>(<scope>): <subject>
```

허용 type은 다음과 같다.

- `feat`
- `fix`
- `docs`
- `refactor`
- `test`
- `chore`
- `build`

예시:

```text
feat(order): add order status transition
test(product): cover stock decrease failure
docs(readme): describe concurrency policy
```
