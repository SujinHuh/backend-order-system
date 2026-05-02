# Plan

## 변경 대상 파일 또는 모듈

- `pom.xml`
- `mvnw`
- `mvnw.cmd`
- `.mvn/**`
- `.gitignore`
- `src/main/java/com/example/ordersystem/**`
- `src/main/resources/application.yml`
- `src/test/java/com/example/ordersystem/**`
- `docs/task/002_scaffold-common-setup/**`

## 레이어별 작업 계획

1. Maven/Spring Boot 스캐폴딩
   - Spring Boot 3 기반 `pom.xml`을 생성한다.
   - Java 17 설정을 추가한다.
   - Maven wrapper를 추가하거나 실행 가능 상태로 만든다.

2. 의존성 구성
   - Spring Web, Spring Data JPA, H2, Validation, Test 의존성을 추가한다.
   - QueryDSL JPA 의존성과 annotation processor 설정을 추가한다.
   - QueryDSL은 Q-class 생성 가능 여부만 확인하고 custom repository는 구현하지 않는다.

3. 기본 애플리케이션 구성
   - `com.example.ordersystem.OrderSystemApplication`을 생성한다.
   - JPA Auditing 설정을 활성화한다.
   - `application.yml`에 H2/JPA 기본 설정을 추가한다.

4. 최소 검증 경로 추가
   - Spring context load 테스트를 추가한다.
   - Q-class 생성 확인이 필요하면 상품/주문 도메인 entity가 아니라 `src/test/java` 아래 테스트 전용 최소 entity 또는 설정 검증 경로로 제한한다.

## 테스트 계획

- `./mvnw test`
- 필요한 경우 `./mvnw compile` 또는 `./mvnw test-compile`로 QueryDSL annotation processing을 확인한다.

## 문서 반영 계획

- 이번 task에서 새 decision 문서는 만들지 않는다.
- QueryDSL 설정 또는 Java/Spring Boot 버전이 문서와 달라지면 `docs/project/standards/architecture.md`와 상위 task 문서를 갱신한다.
- 검증 결과와 미실행 항목은 `validation_report.md`에 남긴다.

## 비범위

- 상품/주문 도메인 entity 구현
- Repository, service, controller 구현
- 공통 예외 처리 구현
- QueryDSL custom repository 구현
- 비즈니스 테스트 작성

## 리스크 또는 확인 포인트

- Spring Boot 3는 Jakarta Persistence를 사용하므로 QueryDSL 의존성 classifier와 annotation processor 설정이 맞아야 한다.
- Maven wrapper 생성에 네트워크 또는 로컬 도구 상태가 영향을 줄 수 있다.
- 도메인 entity를 만들지 않으면 Q-class 생성 확인이 어려울 수 있으므로, 검증용 최소 entity를 만들 경우 `src/test/java` 아래 테스트 전용 타입으로 제한한다.
