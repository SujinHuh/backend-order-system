# Plan

## 변경 대상 파일 또는 모듈

- `src/main/java/com/example/ordersystem/global/error/ErrorCode.java`
- `src/main/java/com/example/ordersystem/global/error/ErrorResponse.java`
- `src/main/java/com/example/ordersystem/global/error/GlobalExceptionHandler.java`
- `src/main/java/com/example/ordersystem/global/error/exception/BusinessException.java`
- `src/main/java/com/example/ordersystem/global/error/exception/EntityNotFoundException.java`
- `src/main/java/com/example/ordersystem/global/error/exception/InvalidValueException.java`
- `src/test/java/com/example/ordersystem/global/error/GlobalExceptionHandlerTest.java`

## 레이어별 작업 계획

### 1. 에러 모델 및 예외 정의
- `ErrorCode` Enum: HTTP 상태 코드, 내부 코드, 기본 메시지 정의
- `ErrorResponse` DTO: 일관된 에러 응답 형식 (code, message, errors) 구현
- `BusinessException`: `RuntimeException` 상속, `ErrorCode` 포함
- 구체적 예외 클래스들(`EntityNotFoundException` 등) 구현

### 2. 전역 예외 처리기 구현
- `GlobalExceptionHandler` 구현 (`@RestControllerAdvice`)
- `BusinessException`, `MethodArgumentNotValidException`, `HttpRequestMethodNotSupportedException`, `Exception` 핸들러 등록
- 로깅 전략: `BusinessException`은 WARN, 일반 예외는 ERROR 레벨로 로그 출력

### 3. 검증용 테스트 작성
- `GlobalExceptionHandlerTest`: `@WebMvcTest`를 사용해 테스트용 Controller를 만들고, 각 예외 상황에서 기대하는 `ErrorResponse`와 HTTP 상태 코드가 반환되는지 검증한다.

## 테스트 계획

- `./mvnw test` 실행
- 테스트 항목:
    - `@Valid` 실패 시 400 에러 및 필드 에러 정보 포함 확인
    - `BusinessException` 발생 시 정의된 `ErrorCode`에 따른 응답 확인
    - 정의되지 않은 예외 발생 시 500 에러 응답 확인

## 문서 반영 계획

- `README.md`에 공통 에러 응답 규격 섹션 추가 (필요 시)

## 비범위

- 개별 도메인 비즈니스 예외 정의 (이후 도메인 구현 시 추가)
- 시큐리티/인증 관련 예외 처리

## 리스크 또는 확인 포인트

- Spring Boot 3에서 `ProblemDetail` 표준을 사용할지, 커스텀 `ErrorResponse`를 고수할지 결정 (요구사항에 따라 커스텀 형식을 우선 사용)
- `MethodArgumentNotValidException`에서 필드 에러 추출 로직 정합성 확인
