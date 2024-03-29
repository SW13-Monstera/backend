# CS-Broker

CS 문제를 공부하고 풀이할 수 있는 CS Broker 서비스를 위한 서버입니다.

## 사용 기술
Spring Boot, Kotlin, Gradle, JPA, Docker, Redis, MariaDB, AWS

## Infra Structure

### 컴포넌트 구성도

![](./doc/img/infra.png)

### CI/CD

![](./doc/img/ci-cd.png)

### VPC 구성도

![](./doc/img/VPC.png)

## 개발 과정

- [Github Actions + AWS를 이용한 Blue Green 배포 세팅하기](https://velog.io/@kshired/Github-Actions-ECR-Auto-Scaling-Group-EC2-CodeDeploy-S3-%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%98%EC%97%AC-BlueGreen-CICD-%EA%B5%AC%EC%B6%95%ED%95%98%EA%B8%B0)
- [Kotlin으로 unit 테스트 작성하기](https://velog.io/@kshired/Spring-Kotlin%EC%9C%BC%EB%A1%9C-unit-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%9E%91%EC%84%B1%ED%95%98%EA%B8%B0)
- [SonarCloud 도입하여 코드 품질 유지하기](https://velog.io/@kshired/Spring-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8%EC%9D%98-%EC%BD%94%EB%93%9C-%ED%92%88%EC%A7%88-%EC%9C%A0%EC%A7%80%EB%A5%BC-%EC%9C%84%ED%95%9C-SonarCloud-%EB%8F%84%EC%9E%85%ED%95%98%EA%B8%B0)
- [Spring Data JPA @Repository 어노테이션 자세히 알아보기](https://velog.io/@kshired/Spring-%EC%99%9C-JPARepository%EB%8A%94-Repository%EA%B0%80-%ED%95%84%EC%9A%94-%EC%97%86%EC%9D%84%EA%B9%8C-deep-dive-%ED%95%B4%EB%B3%B4%EA%B8%B0)
- [AOP를 통하여 반복작업 제거하기](https://velog.io/@kshired/Spring-AOP%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%98%EC%97%AC-%EB%B0%98%EB%B3%B5%EC%9E%91%EC%97%85-%EC%A4%84%EC%9D%B4%EA%B8%B0)
- [Exception을 처리하기 위한 방안](https://velog.io/@kshired/Spring-Exception-%ED%95%B4%EA%B2%B0-%EC%A0%84%EB%9E%B5)
- [CI/CD에서 Github Action을 선택한 이유 및 인프라 구성 살펴보기](https://velog.io/@monstera/Infra-Github-actions%EB%A5%BC-%ED%86%B5%ED%95%B4-CICD-%EC%9D%B8%ED%94%84%EB%9D%BC-%EA%B5%AC%EC%B6%95%ED%95%98%EA%B8%B0)
- [Spring에서 비밀값 관리하기](https://velog.io/@kshired/Spring-yml-%EC%84%A4%EC%A0%95-%ED%8C%8C%EC%9D%BC-%EA%B4%80%EB%A6%AC%ED%95%98%EA%B8%B0)
- [Gitmoji를 커밋 컨벤션으로 사용하기](https://velog.io/@kshired/Gitmoji%EB%A5%BC-%EC%BB%A4%EB%B0%8B-%EC%BB%A8%EB%B2%A4%EC%85%98%EC%9C%BC%EB%A1%9C-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B2%8C-%EB%90%9C-%EC%9D%B4%EC%9C%A0)

## [Documentation](https://docs.csbroker.io/)
