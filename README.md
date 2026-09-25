# job4j_social_media_api

Проект демонстрирует серверную часть социальной сети.

### Центральный процесс дружбы:
  пользователь отправляет заявку в друзья  
→ становится подписчиком  
→ второй пользователь принимает заявку  
→ создаётся дружба  
→ оба пользователя становятся подписчиками друг друга  
→ система отправляет уведомления

### Команда запуска
./mvnm spring-boot:run  
После запуска на странице http://localhost:8080/api/ping получим ответ  

### Подключение к локальной базе данных (PostgreSQL)

Для создания базы данных перед запуском приложения подключитесь к PostgreSQL через терминал:

```bash
psql -h localhost -p 5432 -U postgres
```

После ввода пароля (по умолчанию `password`), создайте базу данных проекта:

```sql
CREATE DATABASE social_media;
```

Для выхода из консоли `psql` введите `\q`.

### Команды проекта

Запуск и остановка окружения  

```bash
docker compose up -d
docker compose down
```

Проверка состояния сервисов

```bash
docker compose ps
```

Запуск приложения и тестов в Linux, macOS

```bash
./mvnw test
./mvnw spring-boot:run
```
Запуск приложения и тестов в Windows

```bash
mvnw.cmd test
mvnw.cmd spring-boot:run
```

### API-контракт
[Ссылка](./docs/api/openapi-single.yaml)

### Краткое описание индексов
idx_offer_friendships_incoming - для получения входящих предложений 
idx_offer_friendships_outgoing - для получения исходящих предложений
idx_friendships_first_user - для поиска друзей, у которых ID больше чем у текущего пользователя
idx_friendships_second_user - для поиска друзей, у которых ID меньше чем у текущего пользователя
idx_subscriptions_follower - для получения подписок пользователя
idx_subscriptions_followed - для получения подписчиков пользователя