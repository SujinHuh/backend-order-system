# Requirements

## 1. 공통 에러 응답 (ErrorResponse)
- 응답 필드:
    - `code`: 내부 에러 코드 (String)
    - `message`: 에러 메시지 (String)
    - `errors`: 필드 에러 목록 (Optional, Validation 실패 시 사용)
- `FieldError` 내부 클래스: `field`, `value`, `reason` 포함

## 2. 에러 코드 (ErrorCode)
- Enum으로 관리하며 다음 정보를 포함:
    - `status`: HTTP 상태 코드 (int)
    - `code`: 내부 식별 코드 (String)
    - `message`: 기본 설명 메시지 (String)
- 필수 포함 항목:
    - `INVALID_INPUT_VALUE` (400)
    - `ENTITY_NOT_FOUND` (404)
    - `METHOD_NOT_ALLOWED` (405)
    - `INTERNAL_SERVER_ERROR` (500)
    - `INVALID_TYPE_VALUE` (400)

## 3. 비즈니스 예외 (BusinessException)
- `RuntimeException`을 상속받는 최상위 비즈니스 예외 클래스
- `ErrorCode`를 필드로 가짐
- 구체적 예외 클래스 (예: `EntityNotFoundException`)는 이를 상속받아 정의

## 4. 전역 예외 처리기 (GlobalExceptionHandler)
- `@RestControllerAdvice` 사용
- 다음 예외들에 대한 핸들러 구현:
    - `MethodArgumentNotValidException`: `@Valid` 검증 실패
    - `BusinessException`: 사용자 정의 비즈니스 예외
    - `HttpRequestMethodNotSupportedException`: 지원하지 않는 HTTP Method 호출
    - `Exception`: 그 외 예상치 못한 모든 예외 (500 처리)

## 성공 기준
- 모든 예외 발생 시 정의된 `ErrorResponse` 형식의 JSON이 반환되어야 함.
- HTTP 상태 코드가 `ErrorCode`에 정의된 값과 일치해야 함.
- Validation 실패 시 상세 필드 에러 정보가 `errors` 리스트에 포함되어야 함.
