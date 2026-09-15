### State diagram заявки
@startuml
[*] --> PENDING : Отправить запрос в друзья
PENDING --> ACCEPTED : Принять запрос на добавление в друзья
PENDING --> REJECTED : Отклонить запрос на добавлние в друзья
ACCEPTED --> [*]
REJECTED --> [*]
@enduml


### Таблица допустимых переходов
| Текущее \ новое состояние | Null | PENDING | ACCEPTED | REJECTED |
| --- | --- | --- | --- | --- |
|Null| \ | + | - | - |
|PENDING| - | \ | + | + |
|ACCEPTED| - | - | \ | - |
|REJECTED| - | - | - | \ |

### Результаты команд
SendFriendRequest - Инициатор подписывается на получателя. Формируется заявка со статусом PENDING.
AcceptFriendRequest - Создается дружба и обратная подписка. Статус заявки становится ACCEPTED.
RejectFriendRequest - Подписка инициатора сохраняетсяю Статус заявки становится REJECTED.

### Ошибки переходов
throw new IllegalStateException("FRIEND_REQUEST_ALREADY_PROCESSED");  
throw new IllegalStateException("ACTIVE_FRIEND_REQUEST_ALREADY_EXISTS");  
throw new IllegalArgumentException("FRIEND_REQUEST_NOT_FOUND");
throw new IllegalArgumentException("SELF_FRIEND_REQUEST_NOT_ALLOWED");

### Повторная заявка
Новая заявка допустима, если нет заявки PENDING. Создается как новый экземпляр и не переписывает
историю предыдущей.
После ACCEPTED новая заявка не нужна, пока пользователи остаются друзьями.

### Диаграмма состояний публикации
@startuml
[*] --> DRAFT : создание
DRAFT --> PUBLISHED : публикация
DRAFT --> CLOSED : удаление черновика
PUBLISHED --> BANNED : блокировка
PUBLISHED --> CLOSED : архивация
BANNED --> PUBLISHED : разблокировка
BANNED --> CLOSED : архивация
state CLOSED : терминальное состояние
CLOSED --> [*]
@enduml