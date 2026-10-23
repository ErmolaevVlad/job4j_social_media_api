## Описание контракта API предложения дружбы

POST /api/offer-friendships/create
POST /api/offer-friendships/accept
POST /api/offer-friendships/reject
GET /api/offer-friendships/incoming
GET /api/offer-friendships/outgoing


## Создание запроса на дружбу 
endpoint:
POST /api/offer-friendships/create
Content-Type: application/json

### POST запрос для отправки заявки в друзья

### JSON запроса
{  
"fromUserId" : "1",  
"toUserId" : "7"  
}

### JSON успешного ответа
{  
"id": "123",  
"fromUserId": "1",  
"toUserId": "7",
"status": "PENDING",  
"createdAt": "2026-09-15T10:25:00+07:00"  
}

### HTTP status успешного ответа
201 Created


### Возможные ошибки
Пользователь не найден:  
HTTP status: 404 Not Found  
JSON ответа:  
{  
"error": "USER_NOT_FOUND",  
"message": "Пользователь не найден."  
}

Заявка самому себе:  
HTTP status: 409 Conflict  
JSON ответа:  
{  
"error": "INVALID_RECIPIENT",  
"message": "Вы не можете отправить заявку в друзья самому себе."  
}

Активная заявка уже существует:  
HTTP status: 409 Conflict  
{  
"error": "REQUEST_ALREADY_EXIST",  
"message": "Активная заявка уже существует."  
}

### Описание поведения при повторной отправке того же запроса
Если заявка еще активна (в статусе PENDING) то система вернет ошибку 409 Conflict с сообщение что заявка уже существует.  
Если заявка была отклонена (статус REJECTED) то система создаст новую заявку и вернет HTTP status 201 Created.
