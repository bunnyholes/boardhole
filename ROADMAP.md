# ROADMAP — Problem Details(PB) 메타데이터(예외가 담아야 할 것)

예외는 PB 표준 5필드(type/title/status/detail/instance) 중 다음 메타 정보를 “직접” 제공한다. 나머지(status, instance 등 요청 컨텍스트 관련 값)는 웹 레이어에서 조립한다.

예외가 반드시 제공
- type(조립용): `typeSlug` 문자열만 제공한다. (예: `duplicate-username`)
- title: 국제화된 최종 문자열 (키가 아닌 완성된 문자열)
- detail: 국제화된 최종 문자열 (`ex.getMessage()`에 채워 던진다)
- code: 비즈니스 에러 코드(예: `USER_DUPLICATE_USERNAME`)

예외가 선택적으로 제공(확장 멤버)
- properties: ProblemDetail 확장 속성으로 추가할 값들 (필요할 때만)
  - 예: 유효성 오류 배열(errors), 지원 메서드(supportedMethods), 파라미터 정보 등

웹 레이어가 제공(예외가 제공하지 않음)
- status: 비즈니스 코드 등의 매핑 규칙으로 결정 (HTTP 숫자)
- instance: 요청 경로(요청 컨텍스트 필요)
- path, method, timestamp, traceId: 요청 컨텍스트에서 채움

요약
- 예외는 “국제화된 title/detail + typeSlug + code(+ 선택 properties)”만 제공한다.
- 웹 레이어는 status/instance/요청 컨텍스트/표준 조립을 담당한다.
