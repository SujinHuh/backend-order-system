# Coding Conventions Project

## 목적

이 문서는 주문 시스템 API 서버에서 적용할 Java/Spring 프로젝트 전용 코딩 규칙을 정의한다.

## 활성 언어와 런타임

- Java 17 이상
- Spring Boot 3 이상
- JPA
- Maven

## Phase 2 구현/감사 체크리스트용 요약

- 현재 프로젝트의 활성 언어/런타임: `java`
- bootstrap 출처 또는 기준 언어 문서: `java_coding_conventions_template.md (install-time input; no runtime path)`
- 현재 프로젝트에서 우선 적용하는 핵심 규칙 범주: `naming / modeling / error handling / concurrency / testing`
- 현재 프로젝트의 주요 금지 패턴: `controller repository direct access, entity response exposure, stock mutation outside transaction`
- 현재 Phase 2에서 미해결로 둘 수 없는 언어별 결정 항목: `없음`
- formatter/linter/type checker/test의 실행 명령, 필수 여부, 적용 시점은 `docs/project/standards/quality_gate_profile.md`에 둔다.

## 패키지 규칙

- 도메인별 패키지를 우선 사용한다.
- 상품 관련 코드는 `product` 하위에 둔다.
- 주문 관련 코드는 `order` 하위에 둔다.
- 공통 예외, 설정, 응답 형식은 `global` 하위에 둔다.

## 네이밍 규칙

- 요청 DTO는 `*Request` suffix를 사용한다.
- 응답 DTO는 `*Response` suffix를 사용한다.
- 목록 조회 조건 DTO는 `*SearchCondition` suffix를 사용한다.
- Repository custom interface는 `*RepositoryCustom` suffix를 사용한다.
- Repository custom 구현체는 `*RepositoryImpl` suffix를 사용한다.
- 비즈니스 예외 코드는 대문자 snake case를 사용한다.

## Domain 규칙

- 엔티티의 상태 변경은 가능한 한 엔티티 메서드로 표현한다.
- setter는 기본적으로 사용하지 않는다.
- JPA 기본 생성자는 `protected`로 둔다.
- 생성과 변경은 정적 팩토리 메서드 또는 의미 있는 메서드명으로 제공한다.
- 도메인 객체는 controller DTO를 알지 않는다.

## Service 규칙

- 트랜잭션은 service public 메서드에 둔다.
- 조회 전용 메서드는 `@Transactional(readOnly = true)`를 사용한다.
- 재고 차감/복구와 주문 상태 변경은 하나의 트랜잭션에서 처리한다.
- 존재하지 않는 리소스, 재고 부족, 상태 전이 오류는 명시적 비즈니스 예외로 처리한다.

## Controller 규칙

- URL prefix는 `/api`를 사용한다.
- 요청 검증은 Bean Validation을 사용한다.
- 엔티티를 응답으로 직접 반환하지 않는다.
- 생성 API는 생성된 리소스의 식별자를 응답에 포함한다.

## Error Handling 규칙

- 에러 응답은 공통 형식을 사용한다.

```json
{
  "code": "ERROR_CODE",
  "message": "error message",
  "status": 400
}
```

- 예외 메시지는 사용자가 원인을 이해할 수 있게 작성한다.
- 내부 구현 세부사항은 에러 응답에 노출하지 않는다.

## 금지 패턴

- Controller에서 repository 직접 호출 금지
- Controller에서 트랜잭션 처리 금지
- Entity를 API 응답으로 직접 반환 금지
- 재고 수량을 트랜잭션 밖에서 변경 금지
- 주문 상태를 문자열 리터럴로 비교 금지
- 테스트 없이 주문 상태 전이 규칙 변경 금지
