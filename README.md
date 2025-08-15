# BeautiFlow_BE

## 주요 기능
### 인증 및 유저 관리
- **Kakao OAuth2 기반 JWT 로그인**
- 고객 / 디자이너 / 사장님 **역할(Role) 분리**
- **샵별 전용 로그인 링크**로 매장/디자이너 단위 로그인 처리
- **OAuth 콜백 → loginKey(단기 Redis) → 프론트 교환 → 일회성 삭제** 흐름 적용  

### 실시간 채팅 시스템
- **STOMP 기반 WebSocket**
- **Redis Pub/Sub**로 멀티 서버 브로드캐스트
- 기능: 채팅방 생성/입장/나가기/재입장, 실시간 메시지, 참여자 목록 반영
- **메시지 템플릿 자동 전송** 지원

### 예약 시스템
- **임시 예약 → 확정 예약**의 2단계 구조
- 하나의 통합 API로 다중 요청 처리
- **Redis 분산 락**으로 시간/디자이너 단위 예약 충돌 방지

### 알림 시스템
- **조건 기반 예약/채팅 알림**
  - 예: **읽지 않은 메시지 1시간 이상** 시 SNS 알림
  - 채팅방 생성 시 자동 환영 메시지
  - 예약 전/후 시점에 **템플릿 자동 발송**

### 고객 관리 & 메시지 템플릿
- 고객 리스트, 고객별 채팅/예약 히스토리 조회
- **사용자 그룹(VIP, 자주 방문 등)** 기반 **그룹 메시지 발송**
- **예약 조건·시점(N일 전/후)** 템플릿 자동/수동 발송

## 아키텍처 개요
<img width="1410" height="852" alt="image" src="https://github.com/user-attachments/assets/b3ef35ad-966f-492e-8919-a88c9c355782" />

---

## 기술 스택
- **Language/Framework**: Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA
- **Realtime**: STOMP over WebSocket, Redis Pub/Sub
- **Storage**: RDBMS (Prod: RDS), Redis
- **Build/Deploy**: Gradle, GitHub Actions, systemd, Nginx, EC2
- **Observability**: Micrometer, Prometheus, Discord 알림

---
## Members
| Backend | Backend | Backend | Backend | Backend |
|:--:|:--:|:--:|:--:|:--:|
| <a href="https://github.com/dongjune8931"><img src="https://avatars.githubusercontent.com/u/164463609?v=4" width="120" height="120" /></a><br/><a href="https://github.com/dongjune8931">이동준</a> | <a href="https://github.com/ally010314"><img src="https://avatars.githubusercontent.com/u/177901876?v=4" width="120" height="120" /></a><br/><a href="https://github.com/ally010314">장수연</a> | <a href="https://github.com/Jeong-Ryeol"><img src="https://avatars.githubusercontent.com/u/188818480?v=4" width="120" height="120" /></a><br/><a href="https://github.com/Jeong-Ryeol">정원렬</a> | <a href="https://github.com/Mode1221"><img src="https://avatars.githubusercontent.com/u/81965868?v=4" width="120" height="120" /></a><br/><a href="https://github.com/Mode1221">정성훈</a> | <a href="https://github.com/bbandm"><img src="https://avatars.githubusercontent.com/u/134187969?v=4" width="120" height="120" /></a><br/><a href="https://github.com/bbandm">황지은</a> |

