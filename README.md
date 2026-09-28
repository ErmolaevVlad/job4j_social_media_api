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
### Результат выполнения тестов

При успешном прохождении всех проверок вывод в консоли будет выглядеть следующим образом:
```text
[INFO] Running ru.job4j.socialmediaapi.repository.JdbcOfferFriendshipRepositoryTest
2026-09-28T12:27:46.420+07:00  INFO 18311 --- [social-media-api] [           main] tc.postgres:17                           : Creating container for image: postgres:17
2026-09-28T12:27:47.855+07:00  INFO 18311 --- [social-media-api] [           main] tc.postgres:17                           : Container postgres:17 started in PT1.435779378S
2026-09-28T12:27:47.856+07:00  INFO 18311 --- [social-media-api] [           main] tc.postgres:17                           : Container is started (JDBC URL: jdbc:postgresql://localhost:38155/test?loggerLevel=OFF)
2026-09-28T12:27:48.337+07:00  INFO 18311 --- [social-media-api] [           main] liquibase.changelog                      : Reading from public.databasechangelog
2026-09-28T12:27:48.427+07:00  INFO 18311 --- [social-media-api] [           main] liquibase.ui                             : Running Changeset: db/changelog/changes/001-create-offer-friendships.sql::001::student
2026-09-28T12:27:48.536+07:00  INFO 18311 --- [social-media-api] [           main] liquibase.util                           : Total change sets:            6
2026-09-28T12:27:48.537+07:00  INFO 18311 --- [social-media-api] [           main] liquibase.command                        : Update command completed successfully.
2026-09-28T12:27:48.537+07:00  INFO 18311 --- [social-media-api] [           main] liquibase.ui                             : Liquibase: Update has been successful. Rows affected: 0
2026-09-28T12:27:48.751+07:00  INFO 18311 --- [social-media-api] [           main] .j.s.r.JdbcOfferFriendshipRepositoryTest : Started JdbcOfferFriendshipRepositoryTest in 0.855 seconds
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.897 s -- in ru.job4j.socialmediaapi.repository.JdbcOfferFriendshipRepositoryTest
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Почему PostgreSQL, а не H2
H2 отличается от PostgreSQL синтаксисом и поведением. Репозиторий использует возможности PostgreSQL: тип UUID, TIMESTAMPTZ,
ограничения и конструкцию INSERT ... RETURNING. H2 отличается от PostgreSQL синтаксисом и поведением. Тест может пройти
на H2, а затем завершиться ошибкой в рабочей базе.

### Аннотации тестового класса
* @Testcontainers подключает управление контейнерами к JUnit Jupiter;
* @Container указывает, что поле описывает контейнер теста;
* @ServiceConnection передаёт Spring Boot URL, имя пользователя и пароль контейнера;
* @SpringBootTest создаёт контекст приложения, запускает Liquibase и позволяет внедрить настоящий репозиторий.


### API-контракт
[Ссылка](./docs/api/openapi-single.yaml)

### Краткое описание индексов
idx_offer_friendships_incoming - для получения входящих предложений 
idx_offer_friendships_outgoing - для получения исходящих предложений
idx_friendships_first_user - для поиска друзей, у которых ID больше чем у текущего пользователя
idx_friendships_second_user - для поиска друзей, у которых ID меньше чем у текущего пользователя
idx_subscriptions_follower - для получения подписок пользователя
idx_subscriptions_followed - для получения подписчиков пользователя
