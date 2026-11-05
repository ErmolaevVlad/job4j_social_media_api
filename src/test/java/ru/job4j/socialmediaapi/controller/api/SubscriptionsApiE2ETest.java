package ru.job4j.socialmediaapi.controller.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.job4j.socialmediaapi.controller.dto.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class SubscriptionsApiE2ETest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate dbAssert;

    @AfterEach
    void clearDatabase() {
        dbAssert.execute("DELETE FROM subscriptions");
    }

    @Test
    @DisplayName("Успех - оформление подписки на пользователя")
    void whenSubscribeToUserThenReturn200AndSaveInDb() {
        var followerId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var targetUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                CreateFollowerResponse.class,
                targetUserId
        );
        System.out.println("STATUS: " + response.getStatusCode());
        System.out.println("BODY: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedDto = new CreateFollowerResponse()
                .message("Вы успешно подписались на " + targetUserId.toString());

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto);

        var dbCount = dbAssert.queryForObject(
                "SELECT COUNT(*) FROM subscriptions",
                Integer.class
        );
        assertThat(dbCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Статус 204 - повторная подписка на того же пользователя")
    void whenSubscribeTwiceThenReturn204() {
        var followerId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var targetUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                Void.class,
                targetUserId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        var dbCount = dbAssert.queryForObject(
                "SELECT COUNT(*) FROM subscriptions",
                Integer.class
        );
        assertThat(dbCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Успех - получение списка подписчиков")
    void whenFindFollowersThenReturnList() {
        var followerId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var targetUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", targetUserId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/followers?page=1&limit=20",
                HttpMethod.GET,
                entity,
                FindFollowersResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedItem = new FindFollowersResponseFollowersInner()
                .id(followerId)
                .username(followerId.toString());

        var expectedMeta = new FindIncomingOfferFriendshipsResponseMeta()
                .currentPage(1)
                .totalItems(1L)
                .totalPages(1);

        var expectedDto = new FindFollowersResponse()
                .followers(List.of(expectedItem))
                .meta(expectedMeta);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("followers.id", "followers.followedAt")
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Успех - получение списка подписок")
    void whenFindSubscriptionsThenReturnList() {
        var followerId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var targetUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscriptions?page=1&limit=20",
                HttpMethod.GET,
                entity,
                FindSubscriptionsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedItem = new FindSubscriptionsResponseSubscriptionsInner()
                .id(targetUserId)
                .username(targetUserId.toString());

        var expectedMeta = new FindSubscriptionsResponseMeta()
                .currentPage(1)
                .totalItems(1L)
                .totalPages(1);

        var expectedDto = new FindSubscriptionsResponse()
                .subscriptions(List.of(expectedItem))
                .meta(expectedMeta);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("subscriptions.id", "subscriptions.followedAt")
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Успех - отписка от пользователя")
    void whenUnsubscribeThenReturn204AndDeleteFromDb() {
        var followerId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var targetUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");

        subscribe(followerId, targetUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/unsubscribe/{targetUserId}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                targetUserId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        var dbCount = dbAssert.queryForObject(
                "SELECT COUNT(*) FROM subscriptions",
                Integer.class
        );
        assertThat(dbCount).isZero();
    }

    @Test
    @DisplayName("Ошибка 409 - попытка подписаться на самого себя")
    void whenSubscribeToSelfThenReturn409() {
        var userId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                CreateFollower409Response.class,
                userId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Ошибка 404 - попытка отписаться от пользователя, на которого нет подписки")
    void whenUnsubscribeNonExistentThenReturn404() {
        var userId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var randomTargetId = UUID.randomUUID();

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/unsubscribe/{targetUserId}",
                HttpMethod.DELETE,
                entity,
                CreateFollower404Response.class,
                randomTargetId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка 400 - некорректный формат UUID при подписке")
    void whenSubscribeWithInvalidUuidThenReturn400() {
        var userId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                FindIncomingOfferFriendships400Response.class,
                "not-a-valid-uuid"
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Ошибка 400 - некорректные параметры пагинации при получении подписчиков")
    void whenFindFollowersWithInvalidPaginationThenReturn400() {
        var userId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                "/api/subscriptions/followers?page=0&limit=101",
                HttpMethod.GET,
                entity,
                FindIncomingOfferFriendships400Response.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void subscribe(UUID followerId, UUID targetUserId) {
        var headers = new HttpHeaders();
        headers.set("X-User-Id", followerId.toString());
        var entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(
                "/api/subscriptions/subscribe/{targetUserId}",
                HttpMethod.PUT,
                entity,
                CreateFollowerResponse.class,
                targetUserId
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}