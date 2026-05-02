# Requirements

## 기능 요구사항

- Maven 기반 Spring Boot 3 프로젝트를 생성해야 한다.
- Java 17을 사용하도록 빌드 설정을 구성해야 한다.
- 기본 패키지는 `com.example.ordersystem`으로 둔다.
- `OrderSystemApplication` 애플리케이션 진입점을 생성해야 한다.
- JPA Auditing을 활성화할 수 있어야 한다.
- H2 Database 기반 로컬 실행 설정을 제공해야 한다.
- QueryDSL 의존성과 annotation processor를 구성해야 한다.
- Q-class 생성 가능 여부를 확인할 수 있는 최소 경로를 제공해야 한다.

## 비기능 요구사항 또는 품질 요구사항

- `./mvnw test`로 테스트를 실행할 수 있어야 한다.
- Spring Boot 애플리케이션 context가 최소 설정으로 로드되어야 한다.
- QueryDSL 설정은 이후 `feat/query-apis`에서 custom repository를 구현할 수 있는 구조여야 한다.
- 이번 task에서는 도메인 규칙이나 API 계약을 구현하지 않는다.

## 입력/출력

- 입력:
  - `README.md`
  - `requirements.md`
  - `docs/project/standards/architecture.md`
  - `docs/project/standards/implementation_order.md`
  - `docs/task/001_order-system-api/plan.md`
- 출력:
  - Maven 프로젝트 파일
  - Spring Boot 애플리케이션 진입점
  - 애플리케이션 설정 파일
  - 최소 테스트 파일
  - QueryDSL Q-class 생성 가능 상태

## 제약사항

- 상품/주문 도메인 entity는 이번 task에서 구현하지 않는다.
- 공통 예외 처리기는 이번 task에서 구현하지 않는다.
- 실제 QueryDSL custom repository와 조건 조회는 이번 task에서 구현하지 않는다.
- 운영 DB 구성은 비범위다.

## 예외 상황

- Maven wrapper 또는 의존성 해석이 실패하는 경우
- QueryDSL annotation processor 설정이 Java 17/Spring Boot 3/Jakarta Persistence 조합과 맞지 않는 경우
- H2 설정이 테스트 context load를 방해하는 경우

## 성공 기준

- `./mvnw test`가 통과한다.
- `./mvnw test-compile` 또는 `./mvnw compile`로 QueryDSL annotation processing이 가능한지 확인한다.
- Spring Boot context load 테스트가 통과한다.
- QueryDSL Q-class 생성이 가능한 설정임을 확인한다.
- `pom.xml`에 Java 17, Spring Boot 3, JPA, H2, Validation, Test, QueryDSL 관련 설정이 반영된다.
