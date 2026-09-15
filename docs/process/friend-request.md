endpoint:
POST /api/friend-requests
Content-Type: application/json

POST запрос для отправки заявки в друзья

### JSON запроса
{  
    "senderId" : "userA"  
    "receiverId" : "userB"  
}

### JSON успешного ответа
{  
    "id": "fr_1",  
    "senderId": "userA",  
    "receiverId": "userB",
    "status": "PENDING",  
    "createdAt": "2026-09-15T10:25:00+07:00"  
}

### HTTP status успешного ответа
201 Created


### Возможные ошибки
Заявка самому себе:  
HTTP status: 400 Bad Request  
JSON ответа:  
{  
    "error": "INVALID_RECIPIENT",  
    "message": "Вы не можете отправить заявку в друзья самому себе."  
}

Пользователь не найден:  
HTTP status: 404 Not Found  
JSON ответа:  
{  
    "error": "USER_NOT_FOUND",  
    "message": "Пользователь не найден."  
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
