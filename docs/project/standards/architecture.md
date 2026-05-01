# Architecture

## 목적

이 문서는 주문 시스템 API 서버의 레이어 구조, 도메인 경계, 의존성 방향을 정의한다.

## 시스템 범위

이 프로젝트는 상품과 주문 도메인을 제공하는 Spring Boot 기반 REST API 서버다.

주요 책임은 다음과 같다.

- 상품 등록, 수정, 조회, 카테고리별 조회, 페이징 조회
- 주문 생성, 주문 상태 변경, 주문 조회, 상태별 조회, 기간별 조회
- 주문 상태 변경에 따른 재고 차감 및 복구
- 재고 정합성을 보장하기 위한 트랜잭션과 동시성 제어
- 일관된 예외 응답 제공

## 기술 구조

- Java 17 이상
- Spring Boot 3 이상
- Spring Web
- Spring Data JPA
- QueryDSL
- H2 Database
- Maven

## 패키지 구조

기본 패키지는 `com.example.ordersystem`으로 둔다.

```text
com.example.ordersystem
  ├── OrderSystemApplication
  ├── global
  │   ├── error
  │   └── config
  ├── product
  │   ├── controller
  │   ├── service
  │   ├── domain
  │   ├── repository
  │   └── dto
  └── order
      ├── controller
      ├── service
      ├── domain
      ├── repository
      └── dto
```

## 레이어 책임

### Controller

- HTTP 요청과 응답을 담당한다.
- 요청 DTO validation을 수행한다.
- 도메인 엔티티를 직접 노출하지 않고 응답 DTO로 변환한다.
- 비즈니스 규칙 판단은 service 또는 domain에 위임한다.

### Service

- 애플리케이션 유스케이스를 담당한다.
- 트랜잭션 경계를 정의한다.
- 여러 repository와 domain 객체를 조합한다.
- 주문 생성, 주문 상태 변경, 재고 차감/복구 같은 핵심 흐름을 조율한다.

### Domain

- 상품, 주문, 주문 항목, 주문 상태의 핵심 규칙을 표현한다.
- 재고 증가/감소, 주문 상태 전이 가능 여부 같은 비즈니스 규칙을 메서드로 가진다.
- JPA 엔티티이지만 외부 요청/응답 DTO에 의존하지 않는다.

### Repository

- JPA 기반 영속성 접근을 담당한다.
- 단순 조회는 Spring Data JPA를 사용한다.
- 동적 조건 조회는 QueryDSL custom repository를 사용한다.
- 재고 차감 대상 상품 조회에는 비관적 락을 사용할 수 있다.

### Global

- 공통 예외 타입과 에러 응답 형식을 관리한다.
- 전역 예외 처리기와 공통 설정을 둔다.

## 핵심 도메인 모델

### Product

- 상품명
- 가격
- 재고수량
- 카테고리

상품은 재고 차감과 복구 메서드를 제공한다.

### Order

- 주문 상태
- 주문 항목 목록
- 생성일시
- 수정일시

주문은 하나 이상의 주문 항목을 가진다.

### OrderItem

- 주문 상품
- 주문 수량
- 주문 당시 가격

주문 당시 가격을 저장해 상품 가격이 나중에 변경되어도 주문 내역의 의미가 보존되도록 한다.

### OrderStatus

- `PENDING`: 대기
- `ACCEPTED`: 접수
- `COMPLETED`: 완료
- `CANCELED`: 취소

허용 상태 전이는 다음으로 제한한다.

```text
PENDING -> ACCEPTED
ACCEPTED -> COMPLETED
PENDING -> CANCELED
ACCEPTED -> CANCELED
COMPLETED -> CANCELED
```

주문 생성 시에는 재고를 차감하지 않는다. `COMPLETED`로 변경될 때 주문 항목 수량만큼 재고를 차감하고, `COMPLETED` 상태의 주문이 `CANCELED`로 변경될 때 차감된 재고를 복구한다.

## 의존성 원칙

- Controller는 Service에 의존한다.
- Service는 Domain과 Repository에 의존한다.
- Domain은 Controller DTO, HTTP, Spring Web에 의존하지 않는다.
- Repository는 영속성 조회 책임만 가진다.
- DTO는 경계 번역 용도로 사용하며 도메인 규칙을 담지 않는다.

## 조회 전략

- 상품 목록, 카테고리별 상품 목록, 주문 상태별 조회, 기간별 조회는 페이징을 기본으로 한다.
- 조건 조합이 필요한 조회는 QueryDSL을 사용한다.
- 주문 상세 조회에서 `OrderItem`, `Product` 접근 시 N+1 문제가 생기지 않도록 fetch join 또는 EntityGraph를 검토한다.

## 트랜잭션 경계

- 상품 등록, 수정은 service 메서드 단위 트랜잭션으로 처리한다.
- 주문 생성은 주문과 주문 항목 저장을 하나의 트랜잭션으로 처리한다.
- 주문 상태 변경은 상태 변경과 재고 변경을 하나의 트랜잭션으로 처리한다.
- 주문 완료 처리 시 재고 차감 대상 상품 row에 비관적 락을 적용한다.
- 재고 검증, 재고 차감, 주문 상태 변경은 같은 트랜잭션 안에서 수행한다.
- 여러 상품을 동시에 차감할 때는 상품 ID 기준으로 정렬해 락 획득 순서를 일정하게 유지한다.
