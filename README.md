# 주문 시스템 (Order System)

## 지원자

허수진

## 과제 개요

본 프로젝트는 상품 관리와 주문 처리를 핵심으로 하는 백엔드 시스템입니다. QueryDSL 기반 조건 조회, Fetch Join을 활용한 조회 경로, 비관적 락 기반 재고 정합성 보장, 도메인 규칙 분리를 중심으로 구현했습니다.

핵심 요구사항은 단순 CRUD 구현에 그치지 않고, 주문 상태 변경 과정에서 발생하는 재고 정합성, 트랜잭션 경계, 동시성 문제, 예외 처리, 조회 경로를 함께 고려하는 것입니다.

## 기술 스택

- **언어 및 프레임워크**: Java 17, Spring Boot 3.3.5
- **데이터 저장소**: H2 Database (In-Memory)
- **ORM 및 조회**: Spring Data JPA, QueryDSL 5.1.0
- **빌드 도구**: Maven
- **유틸리티**: Lombok

## 핵심 도메인 모델

### 1. 상품 (Product)
- **속성**: 상품명, 가격, 재고수량, 카테고리
- **카테고리**: `FOOD`, `FASHION`, `ELECTRONICS`, `BOOK`, `ETC`
- **특징**: 재고 수량은 0 미만이 될 수 없으며, 모든 가격은 0 이상이어야 합니다.

### 2. 주문 (Order)
- **상태 전이**:
    - `PENDING` (대기) -> `ACCEPTED` (접수)
    - `ACCEPTED` (접수) -> `COMPLETED` (완료)
    - `PENDING` / `ACCEPTED` / `COMPLETED` -> `CANCELED` (취소)
- **재고 정책**:
    - 주문 생성(`PENDING`) 시점에는 재고를 차감하지 않습니다.
    - 주문이 **`COMPLETED`** 상태로 전이될 때 실제 재고를 차감합니다.
    - `COMPLETED` 상태에서 `CANCELED`로 전이될 때 재고를 복구합니다.
- **제약**: 동일 상태로의 전이나 비정상적인 흐름(취소 후 완료 등)은 금지됩니다.

## 주요 API 명세

### 상품 API
- `POST /api/products`: 신규 상품 등록
- `PUT /api/products/{id}`: 상품 정보 수정
- `GET /api/products/{id}`: 상품 상세 조회
- `GET /api/products`: 상품 목록 조회 (카테고리 필터링, 페이징, 정렬 지원)
    - 예: `/api/products?category=FOOD&page=0&size=10&sort=id,desc`

### 주문 API
- `POST /api/orders`: 주문 생성 (초기 상태 `PENDING`)
- `PATCH /api/orders/{id}/status`: 주문 상태 변경 (상태 전이 및 재고 처리 수반)
- `GET /api/orders/{id}`: 주문 상세 조회 (주문 항목 및 상품 정보 Fetch Join)
- `GET /api/orders`: 주문 목록 조회 (상태, 기간 필터링, 페이징, 정렬 지원)
    - **기간 조회 조건**: `from` 및 `to`는 ISO local date-time 형식이며, **경계값을 포함(Inclusive)**하여 조회합니다 (`createdAt >= from AND createdAt <= to`).
    - 예: `/api/orders?status=COMPLETED&from=2026-05-01T00:00:00&to=2026-05-31T23:59:59&page=0&size=10&sort=createdAt,desc`

## 기술적 해결 방안

### 1. 데이터 정합성 및 동시성 제어
- **비관적 락(Pessimistic Write Lock)**: 주문 상태 변경 시 대상 주문 row를 비관적 락으로 조회합니다. 재고 변경이 필요한 상태 전이에서는 주문 항목의 상품 row도 비관적 락으로 조회해 동시 재고 수정을 방지합니다.
- **데드락 방지**: 여러 상품의 락을 획득할 때 **상품 ID 순으로 정렬**하여 락을 획득함으로써 순환 대기 가능성을 낮춥니다.

### 2. 조회 성능 최적화
- **QueryDSL 활용**: 복잡한 동적 쿼리(주문 상태 + 기간 필터링)를 타입 안정성을 보장하며 구현했습니다.
- **Fetch Join**: 주문 상세 조회 시 N+1 문제를 방지하기 위해 `OrderItem`과 `Product`를 한 번에 조회합니다.
- **2단계 페이징 전략**: 1:N 관계의 페이징 조회 시 발생할 수 있는 메모리 과부하 및 카테시안 곱 문제를 해결하기 위해, 먼저 ID 목록을 조회한 뒤 해당 ID들에 대해 Fetch Join을 수행합니다.

### 3. 예외 처리 아키텍처
- `GlobalExceptionHandler`를 통해 전역적으로 예외를 관리하며, 일관된 `ErrorResponse` 형식을 반환합니다.
- **대표 예외 상황**:
    - `C001 (INVALID_INPUT_VALUE)`: 재고 부족, 잘못된 가격/수량, 유효하지 않은 주문 상태 전이 등
    - `C003 (ENTITY_NOT_FOUND)`: 존재하지 않는 상품/주문 조회
    - `C005 (INVALID_TYPE_VALUE)`: 잘못된 형식의 날짜 또는 Enum 값 입력
- **에러 응답 예시**:
```json
{
  "status": 400,
  "code": "C001",
  "message": "Invalid Input Value",
  "errors": [
    {
      "field": "stockQuantity",
      "value": "-1",
      "reason": "Stock quantity must be 0 or greater"
    }
  ]
}
```

## 실행 및 테스트

### 실행 방법
```bash
./mvnw spring-boot:run
```

### 테스트 실행
```bash
./mvnw test
```
- 단위 테스트, 서비스 레이어 테스트, MockMvc 기반의 API 통합 테스트를 포함합니다.
- 동시성 테스트를 통해 다중 쓰레드 환경에서의 재고 차감 정합성을 검증합니다.

## 프로젝트 구조 및 상세 문서

- [요구사항 명세](requirements.md)
- [아키텍처 가이드](docs/project/standards/architecture.md)
- [테스트 전략](docs/project/standards/testing_profile.md)
- [결정 기록(ADR)](docs/project/decisions/README.md)
- [태스크 히스토리](docs/task/)
