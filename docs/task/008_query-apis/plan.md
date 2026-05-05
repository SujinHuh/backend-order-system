# Plan

## 변경 대상 파일 또는 모듈

- `src/main/java/com/example/ordersystem/product/domain/ProductRepository.java`
- `src/main/java/com/example/ordersystem/product/domain/ProductRepositoryCustom.java`
- `src/main/java/com/example/ordersystem/product/domain/ProductRepositoryImpl.java`
- `src/main/java/com/example/ordersystem/product/service/ProductService.java`
- `src/main/java/com/example/ordersystem/product/controller/ProductController.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderRepository.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderRepositoryCustom.java`
- `src/main/java/com/example/ordersystem/order/domain/OrderRepositoryImpl.java`
- `src/main/java/com/example/ordersystem/order/service/OrderService.java`
- `src/main/java/com/example/ordersystem/order/controller/OrderController.java`
- `src/test/java/com/example/ordersystem/product/**`
- `src/test/java/com/example/ordersystem/order/**`
- `docs/task/008_query-apis/*`

## 레이어별 작업 계획

### 1. Repository

- `ProductRepositoryCustom`과 `ProductRepositoryImpl`을 추가한다.
- `ProductRepository`가 custom repository를 확장하도록 변경한다.
- 상품 카테고리 조건과 페이징을 QueryDSL로 구현한다.
- `OrderRepositoryCustom`과 `OrderRepositoryImpl`을 추가한다.
- `OrderRepository`가 custom repository를 확장하도록 변경한다.
- 주문 목록의 `status`, `from`, `to` 조건 조합과 페이징을 QueryDSL로 구현한다.
- 주문 목록 응답 매핑에서 `OrderItem.product` 접근으로 N+1 또는 lazy loading 문제가 발생하지 않도록 목록 조회 경로의 fetch 전략을 함께 구현한다.
- 주문 상세 조회용 fetch join 또는 EntityGraph 조회 메서드를 추가한다.
- 기존 `findByIdForUpdate` 비관적 락 조회는 유지한다.

### 2. Service

- `ProductService.getProducts`에 optional category 조건을 반영한다.
- `OrderService.getOrder`가 N+1 방지 조회 경로를 사용하도록 조정한다.
- `OrderService`에 주문 목록 조건 조회 메서드를 추가한다.
- `OrderService`의 주문 목록 응답 변환이 repository의 목록 조회 fetch 전략을 사용하는지 확인한다.
- `from`, `to` 조건은 명시된 inclusive predicate 그대로 repository 조회 조건에 전달한다.

### 3. Controller

- `ProductController.getProducts`에 optional `category` request parameter를 추가한다.
- `OrderController`에 `GET /api/orders/{id}`를 추가한다.
- `OrderController`에 `GET /api/orders`를 추가한다.
- `status`, `from`, `to`, `Pageable` query parameter를 service로 전달한다.
- `from`, `to`는 `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)`를 사용한다.

### 4. Test

- Product repository/service/controller 테스트에 카테고리 조건 조회를 추가한다.
- Order repository 통합 테스트에 상태, 기간, 상태+기간 조건 조회를 추가한다.
- Order repository 통합 테스트에 주문 목록 조회 결과에서 주문 항목과 상품 정보 접근이 정상 동작하는지 검증한다.
- Order controller 테스트에 단건 조회, 목록 조회, 조건 조회 요청/응답 계약을 추가한다.
- 주문 상세 조회에서 주문 항목과 상품 정보가 응답에 포함되는지 검증한다.
- 주문 목록 조회에서 주문 항목과 상품 정보가 응답에 포함되는지 검증한다.

## 테스트 계획

- Repository 통합 테스트:
  - 상품 카테고리별 조회
- 주문 상태별 조회
- 주문 기간별 조회
- 주문 상태/기간 조건 조합 조회
- 주문 상세 조회 시 주문 항목과 상품 fetch 전략 검증
- 주문 목록 조회 시 주문 항목과 상품 fetch 전략 검증
- Service 테스트:
  - 조회 조건 조립
  - 존재하지 않는 주문 조회 실패
  - 기간 조건 조합 조회
- Controller 테스트:
  - `GET /api/products?category=...`
  - `GET /api/orders/{id}`
  - `GET /api/orders?status=...&from=...&to=...`
- 최종 품질 게이트:
  - `./mvnw test`

## 문서 반영 계획

- Phase 2/3 진행 중 구현 순서와 위임 책임을 `implementation_notes.md`에 기록한다.
- Phase 4에서 자동/수동 검증과 미실행 검증을 `validation_report.md`에 기록한다.
- 새 decision 생성은 계획하지 않는다.
- README 최종 반영은 `docs/final-readme` 범위로 유지한다.
- 단, Phase 4에서 실제 구현과 기존 `architecture.md`, `testing_profile.md`가 불일치하면 Phase 5 반영 후보로 기록한다.

## 비범위

- 최종 README 작성
- 주문 상태 변경/재고/동시성 정책 변경
- 예외 코드 세분화
- 상품 등록/수정 DTO validation 구조 개편
- 운영 DB/Testcontainers 도입
- DB migration 도구 도입

## 리스크 또는 확인 포인트

- `OrderResponse.from`은 `OrderItem.product`에 접근하므로 주문 상세/목록 조회 경로에서 fetch join 또는 EntityGraph가 누락되면 N+1 또는 lazy loading 문제가 발생할 수 있다.
- QueryDSL count query와 fetch join을 함께 사용할 때 페이징 구현이 복잡해질 수 있으므로 목록 조회와 상세 조회 전략을 분리한다.
- Spring MVC enum/date-time binding 오류 응답은 기존 전역 예외 처리 범위와 맞춰 확인해야 한다.
- `from > to`를 별도 오류로 처리하는 API 계약은 repo-local 근거가 부족하므로 이번 태스크에서 추가하지 않는다. 구현은 `createdAt >= from`, `createdAt <= to` 조건을 그대로 조합한다.

## Phase 1 내부 감사 결과

### Requirements Audit

- `issue.md`의 상품 카테고리 조건 조회, 주문 단건/목록/상태/기간 조회, QueryDSL custom repository, 페이징, N+1 방지 요청사항이 `requirements.md`에 반영되었다.
- 입력/출력, 제약사항, 예외 상황, 성공 기준이 구현 가능한 수준으로 정리되었다.
- `from > to` 별도 오류 정책은 repo-local 근거가 부족해 신규 API 실패 계약으로 확정하지 않고, 명세된 inclusive predicate 조합만 요구사항으로 유지했다.
- 판정: 승인 가능

### Plan Audit

- `plan.md`는 repository -> service -> controller -> test 순서로 요구사항 구현 계획을 포함한다.
- 테스트 계획과 문서 반영 계획, 비범위, 리스크가 포함되었다.
- 새 decision 생성이 필요 없다는 판단과 Phase 4/5 확인 계획이 포함되었다.
- 최종 README, 동시성 정책 변경, 예외 코드 개편 등 범위 밖 작업은 제외되었다.
- repo-local 근거가 없는 `from > to` 400 정책을 계획에서 제거했다.
- 판정: 승인 가능

### Issue 대비 Plan 누락 감사

- `issue.md`의 요청사항은 변경 대상 파일, 레이어별 작업 계획, 테스트 계획에 반영되었다.
- `issue.md`의 비범위는 `plan.md`에서도 유지된다.
- Controller repository 직접 호출 금지, entity 응답 직접 반환 금지, custom repository naming 제약이 계획에 반영되었다.
- 판정: 승인 가능
