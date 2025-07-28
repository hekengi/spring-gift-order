# spring-gift-order

## Step1 - 카카오 로그인 

#### 기능 요구 사항
- 카카오 로그인을 통해 인가 코드를 받고, 인가 코드를 사용해 토큰을 받은 후 향후 카카오 API 사용을 준비한다.

- 카카오계정 로그인을 통해 인증 코드를 받는다.
- 토큰 받기를 읽고 액세스 토큰을 추출한다.
- 앱 키, 인가 코드가 절대 유출되지 않도록 한다.
  - 특히 시크릿 키는 GitHub나 클라이언트 코드 등 외부에서 볼 수 있는 곳에 추가하지 않는다.
---
## 체크리스트

#### 1. 프로젝트 설정
- [x] `application.properties`에 카카오 REST API 키 (하드코딩 ❌ 환경변수로 등록함), Redirect URI 등 민감 정보 등록
- [x] 민감 정보는 깃허브에 노출되지 않도록 `.gitignore` 및 환경변수 관리

#### 2. DTO 작성
- [x] 카카오 토큰 응답을 받을 DTO 클래스 생성
  #### 응답 DTO 예시
  ```
  {
  "access_token": "ACCESS_TOKEN",
  "token_type": "bearer",
  "refresh_token": "REFRESH_TOKEN",
  "expires_in": 21599,
  "scope": "talk_message",
  "refresh_token_expires_in": 5183999
  }
  ```
- [x] 카카오 사용자 정보를 받을 DTO 클래스 생성

#### 3. Service 구현
- [x] 카카오 인가 코드로 토큰을 요청하는 메서드 구현 (RestClient)
- [x] 사용자 정보 API 호출
- [x] 예외 처리(에러 응답, 네트워크 오류 등) 구현

#### 4. Controller 구현
- [x] 인가 코드 파라미터를 받아 Service로 전달

#### 5. HTML 구현
- [X] 카카오 로그인 버튼 추가 (카카오 인증 URL로 리다이렉트)
