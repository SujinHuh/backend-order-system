# Implementation Order

## 목적

이 문서는 Phase 2에서 주문 시스템 API 서버를 어떤 레이어 순서로 구현할지 정의한다.

## 기본 원칙

- 레이어 순서와 세분화 기준은 `docs/project/standards/architecture.md`의 실제 구조와 의존성 방향을 기준으로 한다.
- 의존성 안쪽에서 바깥쪽으로 구현한다.
- 각 레이어는 `테스트 작성 -> 구현 -> 감사` 순서로 진행한다.
- 특정 API 구현 순서는 각 task의 `plan.md`에서 정한다.
- 공통 예외, 도메인 규칙, 트랜잭션 경계는 controller보다 먼저 확정한다.

## 프로젝트 권장 순서

1. 프로젝트 스캐폴딩
   - Maven 기반 Spring Boot 프로젝트 생성
   - Java 17, Spring Boot 3, JPA, H2, QueryDSL 설정
   - QueryDSL 의존성, annotation processor, Q-class 생성 가능 여부 확인
   - 공통 패키지 구조 생성

2. 공통 기반
   - 공통 에러 응답
   - 비즈니스 예외 타입
   - 전역 예외 처리기
   - validation 설정

3. 도메인 모델
   - `Product`
   - `Order`
   - `OrderItem`
   - `Category`
   - `OrderStatus`
   - 상태 전이, 재고 차감/복구, 주문 항목 생성 규칙

4. Repository
   - Product JPA repository
   - Order JPA repository
   - QueryDSL custom repository
     - 의존성과 Q-class 생성 확인은 스캐폴딩 단계에서 수행한다.
     - 실제 조건 조합 조회와 custom repository 구현은 `feat/query-apis` task에서 수행한다.
   - 재고 차감용 lock 조회 메서드

5. Service
   - 상품 등록, 수정, 조회
   - 주문 생성
   - 주문 상태 변경
   - 재고 차감/복구 트랜잭션
   - 조회 조건 조립

6. Controller
   - 상품 REST API
   - 주문 REST API
   - 요청 DTO validation
   - 응답 DTO 매핑

7. 통합 테스트 및 문서화
   - 핵심 API 통합 테스트
   - 동시성 테스트
   - README 실행 방법 및 설계 설명 보강

## 레이어 세분화 기준

- 상품과 주문은 별도 도메인 패키지로 분리한다.
- 주문 상태 변경과 재고 변경은 주문 service 안의 하나의 유스케이스로 묶는다.
- QueryDSL 조회 구현은 repository custom 구현으로 분리하되, 기능별 구현 중에는 기본 JPA 조회와 custom 조건 조회의 책임을 task별로 나눈다.
- 공통 예외 처리는 `global.error`에 둔다.

## 기록 규칙

- 실제 구현 중 선택한 레이어 순서와 이유는 task의 `implementation_notes.md`에 기록한다.
- 계획과 다른 순서로 진행하면 변경 이유와 영향 범위를 함께 기록한다.
