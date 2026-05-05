# Requirements

## 기능 요구사항

### 1. 상품 조건 조회

- `GET /api/products`는 기존 페이징 목록 조회를 유지한다.
- `category` query parameter가 없으면 전체 상품을 페이징 조회한다.
- `category` query parameter가 있으면 해당 카테고리 상품만 페이징 조회한다.
- 정렬과 페이징은 기존 `Pageable` 계약을 따른다.
- 조건 조회 구현은 QueryDSL custom repository를 사용한다.

### 2. 주문 단건 조회

- `GET /api/orders/{orderId}`를 제공한다.
- 존재하지 않는 주문이면 기존 not found 예외 정책을 따른다.
- 응답은 기존 `OrderResponse` 형식을 사용한다.
- 주문 항목과 상품 정보 접근 시 N+1 문제가 발생하지 않도록 조회 전략을 적용한다.

### 3. 주문 목록 조건 조회

- `GET /api/orders`를 제공한다.
- `status` query parameter를 선택적으로 지원한다.
- `from` query parameter를 선택적으로 지원한다.
- `to` query parameter를 선택적으로 지원한다.
- `page`, `size`, `sort`는 Spring Data `Pageable` 계약을 따른다.
- `status`, `from`, `to`는 함께 조합될 수 있다.
- `from`은 `createdAt >= from` 조건으로 해석한다.
- `to`는 `createdAt <= to` 조건으로 해석한다.
- `from`, `to`는 ISO local date-time 형식으로 받는다.
- 조건 조회 구현은 QueryDSL custom repository를 사용한다.
- 주문 목록 응답은 기존 `OrderResponse` 형식을 `Page<OrderResponse>` 형태로 반환한다.

### 4. QueryDSL custom repository

- 상품 조건 조회는 `ProductRepositoryCustom`과 `ProductRepositoryImpl`로 구현한다.
- 주문 조건 조회는 `OrderRepositoryCustom`과 `OrderRepositoryImpl`로 구현한다.
- QueryDSL Q-class를 활용한다.
- 단순한 락 조회와 기존 Spring Data JPA 조회는 유지한다.

## 비기능 요구사항 또는 품질 요구사항

- 목록 조회는 페이징을 필수로 적용한다.
- 주문 상세 조회 또는 목록 조회에서 `OrderItem`, `Product` 접근으로 인한 명백한 N+1을 방지한다.
- QueryDSL custom repository는 repository layer 책임으로 둔다.
- Service는 조회 조건 조립과 repository 호출을 담당한다.
- Controller는 HTTP query parameter를 service 호출 인자로 변환하는 책임만 가진다.

## 입력/출력

### 상품 목록 조회

- Endpoint: `GET /api/products`
- Query parameters:
  - `category`: optional, `Category`
  - `page`: optional
  - `size`: optional
  - `sort`: optional
- Response: `Page<ProductResponse>`

### 주문 단건 조회

- Endpoint: `GET /api/orders/{orderId}`
- Response: `OrderResponse`

### 주문 목록 조회

- Endpoint: `GET /api/orders`
- Query parameters:
  - `status`: optional, `OrderStatus`
  - `from`: optional, ISO local date-time
  - `to`: optional, ISO local date-time
  - `page`: optional
  - `size`: optional
  - `sort`: optional
- Response: `Page<OrderResponse>`

## 제약사항

- 기존 상품 등록/수정/단건 조회 API 계약은 변경하지 않는다.
- 기존 주문 생성/상태 변경 API 계약은 변경하지 않는다.
- 주문 상태 변경 비관적 락 조회 메서드는 변경하지 않는다.
- 최종 README 정리는 이번 태스크에서 수행하지 않는다.
- QueryDSL 외에 별도 검색 라이브러리를 추가하지 않는다.
- 이번 태스크에서 DB migration 도구를 추가하지 않는다.

## 예외 상황

- 존재하지 않는 상품 단건 조회는 기존 `EntityNotFoundException` 흐름을 유지한다.
- 존재하지 않는 주문 단건 조회는 `EntityNotFoundException`으로 처리한다.
- 잘못된 enum query parameter는 Spring MVC binding/validation 흐름을 따른다.
- 잘못된 date-time query parameter는 Spring MVC binding/validation 흐름을 따른다.
- `from`이 `to`보다 늦은 경우에 대한 별도 400 API 계약은 이번 태스크에서 새로 추가하지 않는다.
- 기간 조건은 명세된 inclusive predicate(`createdAt >= from`, `createdAt <= to`)를 그대로 적용한다.

## 성공 기준

- `GET /api/products?category=FOOD`가 해당 카테고리 상품만 페이징 반환한다.
- `GET /api/products`의 기존 전체 페이징 조회가 유지된다.
- `GET /api/orders/{orderId}`가 주문 상세를 반환한다.
- `GET /api/orders`가 전체 주문을 페이징 반환한다.
- `GET /api/orders?status=COMPLETED`가 상태 조건으로 필터링한다.
- `GET /api/orders?from=...&to=...`가 생성일시 inclusive 조건으로 필터링한다.
- `status`, `from`, `to` 조합 조건이 함께 동작한다.
- 주문 상세/목록 응답에서 주문 항목과 상품 정보가 정상 매핑된다.
- QueryDSL custom repository 통합 테스트가 통과한다.
- Controller 요청/응답 테스트가 통과한다.
- `./mvnw test`가 통과한다.

## Decision 반영 후보

- 현재 조회 전략은 이미 `architecture.md`와 `docs/task/001_order-system-api`에 정의되어 있다.
- QueryDSL custom repository 책임 배치도 기존 프로젝트 표준에 정의되어 있으므로 새 decision 생성은 필요하지 않다.
- `from > to` 별도 오류 정책은 repo-local 근거가 부족하므로 이번 태스크에서 신규 decision 없이 API 계약으로 확정하지 않는다.
- Phase 4/5에서 실제 구현이 `architecture.md`, `testing_profile.md`, README와 불일치하는지 확인한다.
