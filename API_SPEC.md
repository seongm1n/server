# 콘서트 예약 서비스 API 명세서

## 1. 대기열 토큰 발급
**POST** `/api/queue/token`
```json
// Request
{ "userId": "uuid" }

// Response  
{ "token": "string", "queuePosition": 0, "status": "WAITING" }
```

## 2. 대기열 상태 조회
**GET** `/api/queue/status`
```json
// Header: Queue-Token
// Response
{ "queuePosition": 0, "status": "ACTIVE" }
```

## 3. 예약 가능 날짜 조회
**GET** `/api/concerts/dates`
```json
// Header: Queue-Token  
// Response
{ "dates": ["2024-03-01", "2024-03-02"] }
```

## 4. 예약 가능 좌석 조회
**GET** `/api/concerts/seats?date=2024-03-01`
```json
// Header: Queue-Token
// Response
{ "availableSeats": [1, 2, 5, 10] }
```

## 5. 좌석 예약
**POST** `/api/reservations`
```json
// Header: Queue-Token
// Request
{ "userId": "uuid", "date": "2024-03-01", "seatNumber": 1 }

// Response
{ "reservationId": "uuid", "expiresAt": "2024-03-01T10:35:00Z", "price": 50000 }
```

## 6. 잔액 충전
**POST** `/api/balance/charge`
```json
// Header: Queue-Token
// Request
{ "userId": "uuid", "amount": 100000 }

// Response
{ "balance": 150000 }
```

## 7. 잔액 조회
**GET** `/api/balance?userId=uuid`
```json
// Header: Queue-Token
// Response
{ "balance": 150000 }
```

## 8. 결제
**POST** `/api/payments`
```json
// Header: Queue-Token
// Request
{ "userId": "uuid", "reservationId": "uuid" }

// Response
{ "paymentId": "uuid", "status": "COMPLETED" }
```