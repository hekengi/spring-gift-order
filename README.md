# spring-gift-order

## Step2 - 주문하기 

### 기능 요구 사항
카카오톡 메시지 API를 사용하여 주문하기 기능을 구현한다. (나에게 보내기 기능)

- 주문할 때 수령인에게 보낼 메시지를 작성할 수 있다.
- 상품 옵션과 해당 수량을 선택하여 주문하면 해당 상품 옵션의 수량이 차감된다.
- 해당 상품이 위시 리스트에 있는 경우 위시 리스트에서 삭제한다.
- 나에게 보내기를 읽고 주문 내역을 카카오톡 메시지로 전송한다.
- 메시지는 메시지 템플릿의 기본 템플릿이나 사용자 정의 템플릿을 사용하여 자유롭게 작성한다.
- 아래 예시와 같이 HTTP 메시지를 주고받도록 구현한다.

#### Request
```
POST /api/orders HTTP/1.1
Authorization: Bearer {token}
Content-Type: application/json

{
"optionId": 1,
"quantity": 2,
"message": "Please handle this order with care."
}
```

#### Response
```
HTTP/1.1 201 Created
Content-Type: application/json

{
"id": 1,
"optionId": 1,
"quantity": 2,
"orderDateTime": "2024-07-21T10:00:00",
"message": "Please handle this order with care."
}
```

### 카카오톡 메시지 API 정보

#### API 엔드포인트
| 메서드 | URL | 인증 방식 |
|--------|----------------------------------------------------|--------------|
| POST   | https://kapi.kakao.com/v2/api/talk/memo/default/send | 액세스 토큰 |

#### 권한 설정
| 권한                     | 사전 설정                                           | 카카오 로그인 | 동의항목                        |
|--------------------------|----------------------------------------------------|---------------|---------------------------------|
| 카카오톡 메시지 전송 권한 | 플랫폼 등록<br>카카오 로그인 활성화<br>동의항목 설정 | 필요          | 카카오톡 메시지 전송(talk_message) |

#### 요청 헤더
```
Authorization: Bearer ${ACCESS_TOKEN}
Content-Type: application/x-www-form-urlencoded;charset=utf-8
```

#### 요청 본문 파라미터
| 이름 | 타입 | 설명 | 필수 |
|------|------|------|------|
| template_object | Object | 메시지 구성 요소를 담은 객체 (피드, 리스트, 위치, 커머스, 텍스트, 캘린더 중 하나) | O |

#### 응답 형식
| 이름 | 타입 | 설명 | 필수 |
|------|------|------|------|
| result_code | Integer | 전송 성공 시 0 | O |

#### 주문 완료 메시지 예제 (Feed 템플릿)
```json
{
  "object_type": "feed",
  "content": {
    "title": "주문이 완료되었습니다!",
    "description": "상품: {상품명}\n옵션: {옵션명}\n수량: {수량}개\n메시지: {사용자메시지}",
    "image_url": "https://example.com/order-complete.jpg",
    "image_width": 640,
    "image_height": 640,
    "link": {
      "web_url": "https://yourapp.com/orders/{orderId}",
      "mobile_web_url": "https://yourapp.com/orders/{orderId}"
    }
  },
  "buttons": [
    {
      "title": "주문 확인하기",
      "link": {
        "web_url": "https://yourapp.com/orders/{orderId}",
        "mobile_web_url": "https://yourapp.com/orders/{orderId}"
      }
    }
  ]
}
```

#### 성공 응답 예시
```json
{
  "result_code": 0
}
```

### 구현 시나리오
1. 현재 카카오 로그인 api를 통해 로그인한 상태
2. 상품의 옵션과 수량을 선택해서 주문하기를 요청한다.
3. 해당 상품 옵션의 수량이 주문한만큼 차감된다.
4. 해당 상품이 위시 리스트에 있는 경우 위시리스트에서 삭제된다.
5. 주문 내역을 카카오톡 메시지로 전송한다.
6. "나에게 보내기"가 선제조건

### 구현 체크 리스트

#### 1. 데이터베이스 설계
- [x] Order 엔티티 생성 (주문 정보 저장)
- [x] UserKakaoToken 엔티티 생성 (카카오 토큰 관리)
- [x] ProductOption 엔티티 수정 (수량 차감 로직 추가) -> 이미 구현함
- [x] Wish 엔티티 수정 (주문 시 위시리스트 삭제 확인을 위한 로직) 

#### 2. DTO 설계
- [x] OrderRequestDto (주문 요청)
- [x] OrderResponseDto (주문 응답)
- [x] KakaoMessageRequestDto (카카오 메시지 요청)
- [x] KakaoMessageResponseDto (카카오 메시지 응답)

#### 3. 서비스 구현
- [x] OrderService (주문 비즈니스 로직)
  - [x] 주문 생성
  - [x] 상품 옵션 수량 차감
  - [x] 위시리스트에서 상품 삭제 (수량만큼)
- [x] KakaoMessageService (카카오 메시지 전송)
  - [x] 액세스 토큰 관리
  - [x] 토큰 갱신 로직
  - [x] 메시지 전송 API 호출
  - [x] 에러 처리 및 재시도 로직
  - [x] Feed 템플릿 메시지 구성

#### 4. 컨트롤러 구현
- [x] OrderController (주문 API)
  - [x] POST /api/orders - 주문 생성
  - [x] JWT 인증 적용
  - [x] 요청/응답 검증

#### 5. 카카오 API 통합
- [ ] 카카오톡 메시지 전송 API 연동
- [ ] 토큰 저장 및 관리
- [ ] 토큰 만료 시 갱신 로직
- [ ] 메시지 템플릿 설계 (Feed 템플릿 사용)
- [ ] Content-Type: application/x-www-form-urlencoded 설정
- [ ] template_object 파라미터 구성

#### 6. 예외 처리
- [ ] OrderException (주문 관련 예외)
- [ ] KakaoMessageException (카카오 메시지 전송 예외)
- [ ] GlobalExceptionHandler에 예외 처리 추가
- [ ] result_code 기반 성공/실패 처리

#### 7. 테스트 코드
- [ ] OrderServiceTest (주문 서비스 테스트)
- [ ] KakaoMessageServiceTest (카카오 메시지 서비스 테스트)
- [ ] OrderControllerTest (주문 컨트롤러 테스트)
- [ ] 통합 테스트 (전체 주문 플로우)
- [ ] 카카오 API 모킹 테스트

### 고려사항
- **트랜잭션 관리**: 주문 생성, 수량 차감, 위시리스트 삭제를 하나의 트랜잭션으로 처리
- **토큰 관리**: 카카오 액세스 토큰의 만료 시간 관리 및 갱신 로직
- **에러 처리**: 메시지 전송 실패 시 적절한 에러 처리 및 재시도 로직
- **성능**: 카카오 API 호출 시 타임아웃 설정 및 비동기 처리 고려
- **Content-Type**: application/x-www-form-urlencoded 형식으로 요청 전송
- **메시지 템플릿**: Feed 템플릿 사용 (object_type: "feed")
- **API 응답**: result_code가 0이면 성공, 그 외는 실패로 처리
- **파라미터**: template_object를 JSON 형태로 구성해서 전달
