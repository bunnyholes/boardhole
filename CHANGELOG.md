# Changelog

## 1.0.0 (2025-09-18)


### ⚠ BREAKING CHANGES

* 버저닝 시스템이 Semantic-Release에서 Release-Please로 변경됨
* 모든 엔티티 ID가 Long에서 UUID로 변경됨
* Entities now use Hibernate's @SoftDelete instead of manual soft delete implementation
* First production release
* **i18n:** 메시지 키 변경으로 메시지 파일 업데이트 필요
* 모든 패키지 경로가 변경되어 외부 의존성 업데이트 필요

### Features

* add locale configuration and properties infrastructure ([b35fcc9](https://github.com/BunnyHoles/board-hole/commit/b35fcc916b8b6e85dcfd570b7285d8c6d85dee91))
* add USER role to admin initialization ([1a124cf](https://github.com/BunnyHoles/board-hole/commit/1a124cfd0192a5a7abc35ab966b8056c2f533a8d))
* Admin 모듈에 Command 패턴 적용 ([#4](https://github.com/BunnyHoles/board-hole/issues/4)) ([#11](https://github.com/BunnyHoles/board-hole/issues/11)) ([318c5c1](https://github.com/BunnyHoles/board-hole/commit/318c5c102436cafb86d4100d1098ca16e062170c))
* **api:** add Korean examples; standardize ProblemDetail (type/instance/code/errors) with base-uri option ([ec7e2f4](https://github.com/BunnyHoles/board-hole/commit/ec7e2f486012c3f3831963f1e425cef8583b1873))
* Auth 모듈에 CQRS Query 서비스 구현 ([c133bda](https://github.com/BunnyHoles/board-hole/commit/c133bda8dfe87f89b83af0c275e2de1336f5fcfe))
* Auth 모듈에 CQRS Query 서비스 구현 ([41c5330](https://github.com/BunnyHoles/board-hole/commit/41c5330e24453f52548a52080d10a1d7c2d46cca))
* **ci:** add complete Git Flow CI/CD workflows to master branch ([f09a759](https://github.com/BunnyHoles/board-hole/commit/f09a759aac46df56688a5362bb2790c25eae6359))
* code quality improvements and configuration updates for 1.0.0 release ([b4472e2](https://github.com/BunnyHoles/board-hole/commit/b4472e2c2a6597494341529ffda2e693894cc7a8))
* **config:** Add OpenAPI/Swagger UI configuration to application.yml ([55716b0](https://github.com/BunnyHoles/board-hole/commit/55716b0018f486a947d79488a7c7ad62670a0dfc))
* CQRS 아키텍처 전환 및 완전한 시스템 재구축 ([c56448d](https://github.com/BunnyHoles/board-hole/commit/c56448db82209b3a384a38d4238b3b2f499d00a7))
* enhance OpenAPI documentation with pagination schema for board responses ([e576eab](https://github.com/BunnyHoles/board-hole/commit/e576eab1047ec6398fc4f5dd3b79f719a64b22dd))
* gradle 버전과 application.yml API 버전 자동 연동 구현 ([47e36fc](https://github.com/BunnyHoles/board-hole/commit/47e36fc91b815f0ecfacade1baf6ad881872cece))
* **i18n:** 하드코딩된 문자열 제거 및 국제화 컨벤션 적용 ([89e3604](https://github.com/BunnyHoles/board-hole/commit/89e3604533e1507176811aa6c4193266f5a38e5d))
* implement Hibernate @SoftDelete with Native Query support ([d10ca83](https://github.com/BunnyHoles/board-hole/commit/d10ca83801d554174837a1c8219618be98e36aaa))
* Release-Please 기반 CI/CD 파이프라인 구축 ([#34](https://github.com/BunnyHoles/board-hole/issues/34)) ([58d084e](https://github.com/BunnyHoles/board-hole/commit/58d084eb029d215244d1306dc97aeb313a76a9d6))
* **shared-validation:** 비밀번호 교차 필드 검증 @PasswordConfirmed 추가 및 PasswordUpdateRequest/UpdatePasswordCommand 적용 ([7128f1b](https://github.com/BunnyHoles/board-hole/commit/7128f1bd719ea516bdf03c63ec8ead03e98d5ba9))
* **soft-delete:** enforce @SQLDelete on User/Board and remove custom repository base\n\n- Add @SQLDelete to entities (Board includes version predicate)\n- Add @Where(clause='deleted=false') on entities for query filtering\n- Remove CustomJpaRepositoryImpl and repositoryBaseClass configs ([5228314](https://github.com/BunnyHoles/board-hole/commit/52283142392f63efa013aace263925357097389f))
* Spring Security JWT 인증 시스템 구현 ([fd92dfb](https://github.com/BunnyHoles/board-hole/commit/fd92dfbe2e2e958390dc9f90f850f7597c68d155))
* Thymeleaf 기반 웹 인터페이스 추가 및 아키텍처 개선 ([c5eae62](https://github.com/BunnyHoles/board-hole/commit/c5eae6249a137258d85cc01165989e701054436b))
* **ui:** pagination redesign (« ‹ 1..N › ») fixed controls without layout shift ([ec7e2f4](https://github.com/BunnyHoles/board-hole/commit/ec7e2f486012c3f3831963f1e425cef8583b1873))
* **validation:** 도메인 검증 체계 전면 개선 및 커스텀 애너테이션 추가 ([d15f665](https://github.com/BunnyHoles/board-hole/commit/d15f6652ac46e2fe5967d6d77dbb06eafc3f792a))
* 개선된 템플릿 및 API 설정, 사용자 요청 처리 로직 수정 ([de9e424](https://github.com/BunnyHoles/board-hole/commit/de9e42496a9289fdaf0ed7873dd5fb7a419ec6a1))
* 엔티티 ID를 Long에서 UUID로 마이그레이션 ([8bb5d13](https://github.com/BunnyHoles/board-hole/commit/8bb5d13553da782daec3d41695ac9ffdb63831e4))
* 인증 및 세션 관리 개선 ([948b243](https://github.com/BunnyHoles/board-hole/commit/948b243baa235a2d1d3790e6e8cdb74afdf34d93))
* 품질 검사 도구 통합 및 점진적 개선 전략 적용 ([8f410a4](https://github.com/BunnyHoles/board-hole/commit/8f410a423a348834a10b71957ee4c18489bb30fd))
* 품질 검사 도구 통합 및 점진적 개선 전략 적용 ([#15](https://github.com/BunnyHoles/board-hole/issues/15)) ([1d62771](https://github.com/BunnyHoles/board-hole/commit/1d62771c30d8f40d9b31f19d38652ed8644ef689))
* 회원가입 이메일 인증 시스템 구현 ([#19](https://github.com/BunnyHoles/board-hole/issues/19)) ([#20](https://github.com/BunnyHoles/board-hole/issues/20)) ([fbdacb8](https://github.com/BunnyHoles/board-hole/commit/fbdacb85b506f7b24b38deb42c5699db722d6e89))


### Bug Fixes

* add gradle.properties to git tracking for version management ([0f8a2d5](https://github.com/BunnyHoles/board-hole/commit/0f8a2d568aaac06b34d3aa29ba22d8ac991b2ebb))
* add missing @GetMapping annotation for board list endpoint ([52c399f](https://github.com/BunnyHoles/board-hole/commit/52c399fa92e121b81e4261565ece67994f15b444))
* allow public email verification endpoints ([#27](https://github.com/BunnyHoles/board-hole/issues/27)) ([1861573](https://github.com/BunnyHoles/board-hole/commit/1861573c5128910a0e74adb1faec62a6831a3901))
* **ci:** add write permissions to GitHub Actions workflows ([049636e](https://github.com/BunnyHoles/board-hole/commit/049636e2a3ee8aa691681b78c6a939175e2056cd))
* correct checkstyle configuration - move FileLength module to Checker level ([8ecd1a5](https://github.com/BunnyHoles/board-hole/commit/8ecd1a54611bf6afed56cbe2af15eff1261f9614))
* correct layered architecture rules and resolve test failures ([238909d](https://github.com/BunnyHoles/board-hole/commit/238909d244cd97327180e543c667de04ecc8bcaa))
* **exception:** ValidationException의 @ResponseStatus(BAD_REQUEST) 제거 → 전역 핸들러(422)와 일치 ([7128f1b](https://github.com/BunnyHoles/board-hole/commit/7128f1bd719ea516bdf03c63ec8ead03e98d5ba9))
* secure args and log deletions ([#29](https://github.com/BunnyHoles/board-hole/issues/29)) ([0439bb5](https://github.com/BunnyHoles/board-hole/commit/0439bb5b3cd53ebf734cfd348f624c70b83e2ab7))
* **security:** permit static resources; refine AccessDenied/AuthEntryPoint messages ([ec7e2f4](https://github.com/BunnyHoles/board-hole/commit/ec7e2f486012c3f3831963f1e425cef8583b1873))
* **test:** 테스트 실패 문제 해결 및 안정성 개선 ([abeff3a](https://github.com/BunnyHoles/board-hole/commit/abeff3a1179c6f33f3ecdba76891beba5f1c8646))
* update contact URL in application.yml to point to GitHub repository ([f993732](https://github.com/BunnyHoles/board-hole/commit/f993732de342259cce833359d93bf3eb46d6c18e))
* 기본 역할을 프로퍼티로 분리 ([8d066ed](https://github.com/BunnyHoles/board-hole/commit/8d066ed8052032cf1095032f1503f49da3ee6d87))
* 코드 리뷰 피드백 반영 ([db1fd12](https://github.com/BunnyHoles/board-hole/commit/db1fd12af9f51771004cf7c440f7d5c521741a44))


### Miscellaneous Chores

* prepare for v1.0.0 release ([1f60812](https://github.com/BunnyHoles/board-hole/commit/1f6081226b11f363c42e0cda8a2822a0824e7085))


### Code Refactoring

* 도메인 중심 아키텍처로 패키지 구조 전면 재편 및 코드 현대화 ([e33e29b](https://github.com/BunnyHoles/board-hole/commit/e33e29b24b5ab64bfff81713a4d36f3a55de33e7))
