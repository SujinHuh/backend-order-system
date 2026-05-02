# Plan

## 목표

README와 과제 요구사항을 만족하는 주문 시스템 REST API 서버를 구현한다.

이 문서는 전체 과제의 상위 마스터 task 계획이다. 실제 구현은 기능별 task workspace에서 Phase 1~5를 각각 수행한다.

## 기능별 task 분리

1. `docs/task/002_scaffold-common-setup`
   - 브랜치: `feat/scaffold-common-setup`
   - 범위: Maven/Spring Boot 스캐폴딩, Java 17, JPA, H2, QueryDSL 의존성, Validation, Test, JPA Auditing, 기본 패키지
2. `docs/task/003_global-exception-handler`
   - 브랜치: `feat/global-exception-handler`
   - 범위: 공통 에러 응답, 에러 코드, 비즈니스 예외, 전역 예외 처리
3. `docs/task/004_product-api`
   - 브랜치: `feat/product-api`
   - 범위: 상품 도메인, 상품 등록/수정/단건 조회/기본 목록 조회
4. `docs/task/005_order-create-api`
   - 브랜치: `feat/order-create-api`
   - 범위: 주문 도메인, 주문 생성, 주문 당시 가격 저장, 기본 상태
5. `docs/task/006_order-status-stock`
   - 브랜치: `feat/order-status-stock`
   - 범위: 주문 상태 전이, 재고 차감/복구, 동일 상태 변경 실패
6. `docs/task/007_order-concurrency-control`
   - 브랜치: `feat/order-concurrency-control`
   - 범위: 주문 row와 상품 row 비관적 락, 동시성 테스트
7. `docs/task/008_query-apis`
   - 브랜치: `feat/query-apis`
   - 범위: QueryDSL custom repository, 상품/주문 목록 조건 조회, 페이징, N+1 방지
8. `docs/task/009_final-readme`
   - 브랜치: `docs/final-readme`
   - 범위: 전체 검증 결과, README 실행 방법/API 예시/성능 고려사항 정리

## 구현 순서

1. Spring Boot 프로젝트 스캐폴딩
   - Maven 프로젝트 생성
   - Java 17, Spring Boot 3 설정
   - JPA, H2, QueryDSL, Validation, Test 의존성 구성
   - QueryDSL 의존성과 annotation processor를 설정하고 Q-class 생성 가능 여부를 확인한다.
   - 실제 QueryDSL custom repository 구현은 `feat/query-apis`에서 수행한다.
   - 기본 패키지 생성
   - JPA auditing 설정을 추가해 생성일시와 수정일시를 관리한다.

2. 공통 예외 처리
   - 공통 에러 응답 정의
   - 에러 코드 정의
   - 비즈니스 예외 정의
   - 전역 예외 처리기 구현

3. 상품 도메인 구현
   - `Product`, `Category` 구현
   - `Category` 초기 지원 enum 값은 `FOOD`, `FASHION`, `ELECTRONICS`, `BOOK`, `ETC`로 제한한다.
   - `products` 테이블 매핑: `id`, `name`, `price`, `stockQuantity`, `category`, `createdAt`, `updatedAt`
   - 상품 가격과 재고수량의 음수 방지 규칙 구현
   - 재고 차감/복구 메서드 구현
   - 상품 등록, 수정, 조회 service 구현
   - 상품 repository 및 기본 JPA 조회 구현
   - QueryDSL custom 상품 조회는 `feat/query-apis`에서 구현한다.
   - 상품 controller와 DTO 구현
   - 상품 관련 테스트 작성

4. 주문 도메인 구현
   - `Order`, `OrderItem`, `OrderStatus` 구현
   - `orders` 테이블 매핑: `id`, `status`, `createdAt`, `updatedAt`
   - `order_items` 테이블 매핑: `id`, `order`, `product`, `quantity`, `orderPrice`
   - `Order`와 `OrderItem`은 `@OneToMany`와 `@ManyToOne`으로 연결하고, `Product`와 `Order`의 직접 다대다 매핑은 만들지 않는다.
   - 주문 당시 가격을 `OrderItem.orderPrice`에 저장한다.
   - 주문 생성 구현
   - 주문 상태 전이 규칙 구현
   - 동일 상태 변경 요청을 허용되지 않은 주문 상태 변경으로 처리한다.
   - 주문 완료 시 재고 차감 구현
   - 주문 취소 시 재고 복구 구현
   - 주문 생성과 상태 변경에 필요한 repository 기본 조회 구현
   - 주문 목록 조건 조회와 QueryDSL custom 조회는 `feat/query-apis`에서 구현한다.
   - 주문 controller와 DTO 구현

5. 동시성 처리
   - 주문 상태 변경 대상 주문 row 비관적 락 조회 구현
   - 주문 완료 처리 시 상품 row 비관적 락 조회 구현
   - 완료 주문 취소처럼 재고 복구가 필요한 상태 전이에서도 상품 row 비관적 락 적용
   - 주문 row를 먼저 잠근 뒤 상품 ID 정렬 순서로 상품 row lock 획득
   - 상품 ID 정렬로 lock 획득 순서 안정화
   - 재고 검증, 재고 차감, 주문 상태 변경을 하나의 transaction 안에서 수행
   - 동시 주문 완료 테스트 작성

6. 통합 테스트
   - 핵심 API happy path 검증
   - 필수 failure path 검증
   - 주문 상태 변경과 재고 정합성 검증

7. 문서화
   - README 실행 방법 보강
   - API 요청/응답 예시 보강
   - 동시성 처리 방식 보강
   - 성능 고려사항 보강

## 테스트 계획

- 도메인 단위 테스트
  - 상품 가격/재고 음수 방지
  - 주문 상태 전이 가능 여부
  - 동일 상태 변경 요청 실패
  - 재고 차감과 복구
  - 재고 부족 예외
  - 주문 항목 수량 1 이상 검증

- Service 통합 테스트
  - 주문 생성
  - 주문 완료 시 재고 차감
  - 주문 취소 시 재고 복구
  - 존재하지 않는 상품 주문 실패
  - 허용되지 않은 상태 변경 실패

- Repository 통합 테스트
  - 상품 카테고리별 조회
  - 주문 상태별 조회
  - 주문 기간별 조회
  - 주문 상태/기간 조건 조합 조회
  - 주문 상세 조회 시 주문 항목과 상품 조회 전략 검증
  - 주문 상태 변경 대상 주문 비관적 락 조회
  - 재고 차감 대상 상품 비관적 락 조회

- 동시성 테스트
  - 같은 상품에 대해 동시에 주문 완료 요청을 수행해 재고가 음수가 되지 않는지 확인
  - 같은 주문에 대해 동시에 완료 요청을 수행해 재고가 중복 차감되지 않는지 확인

## 문서 반영 계획

- 구현 결과에 따라 `README.md`의 실행 방법과 API 예시를 최신화한다.
- 실제 entity/table 매핑이 문서의 데이터베이스 모델과 달라지면 `README.md`, `docs/project/standards/architecture.md`, `docs/task/001_order-system-api/requirements.md`를 함께 갱신한다.
- 상태 전이 또는 재고 정책이 바뀌면 `README.md`, `docs/project/standards/architecture.md`, `requirements.md`를 함께 갱신한다.
- 동시성 전략이 바뀌면 `README.md`와 `docs/project/standards/architecture.md`를 함께 갱신한다.
- 상태 전이, 재고 정책, 동시성 전략이 변경되면 관련 `docs/project/decisions/` 문서를 필요 시 갱신한다.
- 검증 결과와 미실행 항목은 `validation_report.md`에 남긴다.

## 비범위

- 인증/인가
- 회원 관리
- 결제
- 배송
- 외부 시스템 연동
- 운영 DB 구성
- CI/CD 구성

## Phase 1 체크

- 요구사항은 `requirements.md`에 구현 가능 단위로 분리했다.
- 구현 순서는 `docs/project/standards/implementation_order.md`의 레이어 순서를 따른다.
- 반복 판단이 필요한 상태 전이와 동시성 정책은 README와 architecture 문서에 직접 남겼다.
