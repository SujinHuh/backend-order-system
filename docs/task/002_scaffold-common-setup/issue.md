# Issue

## 배경

주문 시스템 API 서버 구현을 시작하기 위해 Spring Boot 기반 프로젝트 골격과 공통 개발 설정이 필요하다.

상위 마스터 task는 `docs/task/001_order-system-api`이며, 이번 task는 기능별 구현 이슈 중 첫 번째인 `프로젝트 스캐폴딩 및 공통 설정 추가`에 해당한다.

## 요청사항

- Maven 기반 Spring Boot 3 프로젝트를 생성한다.
- Java 17 기준으로 빌드 설정을 구성한다.
- Spring Web, Spring Data JPA, H2 Database, Bean Validation, Test 의존성을 구성한다.
- QueryDSL 의존성과 annotation processor를 구성한다.
- QueryDSL Q-class 생성 가능 여부를 확인한다.
- JPA Auditing을 활성화한다.
- 기본 패키지 `com.example.ordersystem`과 애플리케이션 진입점을 생성한다.
- 최소한의 테스트 실행 경로를 확보한다.

## 비범위

- 상품/주문 도메인 entity 구현
- 공통 예외 응답과 전역 예외 처리 구현
- 상품 API 구현
- 주문 API 구현
- QueryDSL custom repository 구현
- 동시성 제어 구현
- 최종 제출 README 정리

## 승인 또는 제약 조건

- 실제 QueryDSL custom repository 구현은 `feat/query-apis` task에서 수행한다.
- 이번 task에서 QueryDSL은 의존성, annotation processor, Q-class 생성 가능 여부만 확인한다.
- 구현 기준은 `docs/project/standards/architecture.md`와 `docs/project/standards/implementation_order.md`를 따른다.
