# Issue

## 배경

주문 시스템 전반에서 일관된 에러 응답 형식을 제공하고, 비즈니스 예외 발생 시 적절한 HTTP 상태 코드와 에러 코드를 반환하기 위한 전역 예외 처리 메커니즘이 필요함.

## 요청사항

- 공통 에러 응답 DTO 정의 (에러 코드, 메시지 포함)
- 비즈니스 예외의 최상위 클래스(`BusinessException`) 및 구체적인 예외 상황 정의
- `@RestControllerAdvice`를 활용한 전역 예외 처리기(`GlobalExceptionHandler`) 구현
- Bean Validation(`@Valid`) 실패 시 발생하는 예외를 공통 에러 형식으로 변환
- 표준 에러 코드(Enum) 정의 (예: INVALID_INPUT_VALUE, ENTITY_NOT_FOUND, INTERNAL_SERVER_ERROR 등)

## 비범위

- 개별 도메인(상품, 주문)의 구체적인 비즈니스 로직 구현
- 인증/인가 관련 예외 처리 (별도 태스크)
- 외부 시스템 연동 에러 처리

## 승인 또는 제약 조건

- Spring Boot 3.3+ 및 Java 17 표준을 따를 것
- 모든 에러 응답은 JSON 형식을 유지할 것
- `GlobalExceptionHandler`는 `docs/project/standards/architecture.md`에서 정의한 예외 처리 원칙(있는 경우)을 준수할 것
