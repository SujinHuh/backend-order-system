# Plan

## 목표

README와 과제 요구사항을 만족하는 주문 시스템 REST API 서버를 구현한다.

## 구현 순서

1. Spring Boot 프로젝트 스캐폴딩
   - Maven 프로젝트 생성
   - Java 17, Spring Boot 3 설정
   - JPA, H2, QueryDSL, Validation, Test 의존성 구성
   - 기본 패키지 생성

2. 공통 예외 처리
   - 공통 에러 응답 정의
   - 에러 코드 정의
   - 비즈니스 예외 정의
   - 전역 예외 처리기 구현

3. 상품 도메인 구현
   - `Product`, `Category` 구현
   - 상품 등록, 수정, 조회 service 구현
   - 상품 repository 및 QueryDSL 조회 구현
   - 상품 controller와 DTO 구현
   - 상품 관련 테스트 작성

4. 주문 도메인 구현
   - `Order`, `OrderItem`, `OrderStatus` 구현
   - 주문 생성 구현
   - 주문 상태 전이 규칙 구현
   - 주문 완료 시 재고 차감 구현
   - 주문 취소 시 재고 복구 구현
   - 주문 조회 repository 및 QueryDSL 조회 구현
   - 주문 controller와 DTO 구현

5. 동시성 처리
   - 주문 완료 처리 시 상품 row 비관적 락 조회 구현
   - 상품 ID 정렬로 lock 획득 순서 안정화
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
  - 주문 상태 전이 가능 여부
  - 재고 차감과 복구
  - 재고 부족 예외

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

- 동시성 테스트
  - 같은 상품에 대해 동시에 주문 완료 요청을 수행해 재고가 음수가 되지 않는지 확인

## 문서 반영 계획

- 구현 결과에 따라 `README.md`의 실행 방법과 API 예시를 최신화한다.
- 상태 전이 또는 재고 정책이 바뀌면 `DEC-001`을 갱신한다.
- 동시성 전략이 바뀌면 `DEC-002`를 갱신한다.
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
- 반복 판단이 필요한 상태 전이와 동시성 정책은 decision 문서로 분리했다.
