# Issue

## 배경

`feat/order-concurrency-control`까지 상품 등록/수정/단건 조회/기본 목록 조회, 주문 생성, 주문 상태 변경, 재고 차감/복구, 동시성 제어가 구현되었다.

아직 남은 조회 범위는 다음과 같다.

- 상품 목록의 카테고리 조건 조회
- 주문 단건 조회 API
- 주문 목록 조회 API
- 주문 상태 조건 조회
- 주문 생성 기간 조건 조회
- 주문 목록 조회의 페이징
- QueryDSL custom repository 기반 동적 조건 조회
- 주문 상세 조회 시 `OrderItem`, `Product` 접근의 N+1 방지

프로젝트 문서에서는 실제 QueryDSL custom repository 구현을 `feat/query-apis` 범위로 분리해 두었다.

## 요청사항

- `feat/query-apis` 브랜치에서 상품/주문 조회 API를 구현한다.
- 상품 목록 조회는 기존 `GET /api/products`를 유지하되 `category` query parameter를 선택적으로 지원한다.
- 주문 단건 조회 API `GET /api/orders/{orderId}`를 제공한다.
- 주문 목록 조회 API `GET /api/orders`를 제공한다.
- 주문 목록 조회는 `status`, `from`, `to`, `page`, `size` 조건을 함께 사용할 수 있어야 한다.
- `from`, `to`는 ISO local date-time 형식이며 inclusive 조건으로 해석한다.
- 조건 조합 조회는 QueryDSL custom repository로 구현한다.
- 주문 상세 응답에서 주문 항목과 상품 정보 접근 시 N+1 문제가 발생하지 않도록 fetch join 또는 EntityGraph를 적용한다.
- 목록 조회는 페이징을 기본으로 한다.

## 비범위

- 최종 제출 README 정리는 `docs/final-readme` 범위로 남긴다.
- 주문 상태 변경, 재고 차감/복구, 동시성 정책은 변경하지 않는다.
- 상품 등록/수정 요청 DTO validation 구조 개편은 이번 태스크에서 다루지 않는다.
- 예외 코드 세분화는 이번 태스크에서 다루지 않는다.
- 별도 운영 DB 또는 Testcontainers 도입은 이번 태스크에서 다루지 않는다.
- DB schema migration 도구 도입은 이번 태스크에서 다루지 않는다.

## 승인 또는 제약 조건

- Controller가 repository를 직접 호출하지 않는다.
- Entity를 API 응답으로 직접 반환하지 않는다.
- QueryDSL custom repository naming 규칙을 따른다.
  - `*RepositoryCustom`
  - `*RepositoryImpl`
- 목록 조회는 `Page` 또는 Spring Data 페이징 응답을 유지한다.
- `./mvnw test`가 통과해야 한다.
