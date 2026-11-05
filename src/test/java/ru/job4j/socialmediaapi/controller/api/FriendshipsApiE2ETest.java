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
class FriendshipsApiE2ETest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate dbAssert;

    @AfterEach
    void clearDatabase() {
        dbAssert.execute("DELETE FROM friendships");
        dbAssert.execute("DELETE FROM offer_friendships");
    }

    @Test
    @DisplayName("Успех - получение списка друзей")
    void whenFindFriendshipsThenReturnList() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        createFriendshipBetween(fromUserId, toUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", fromUserId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/friends/friends?page=1&limit=20",
                HttpMethod.GET,
                entity,
                FindFriendshipsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedFriend = new FindFriendshipsResponseFriendsInner()
                .id(toUserId)
                .username(toUserId.toString());

        var expectedMeta = new FindFriendshipsResponseMeta()
                .currentPage(1)
                .totalItems(1L)
                .totalPages(1);

        var expectedDto = new FindFriendshipsResponse()
                .friends(List.of(expectedFriend))
                .meta(expectedMeta);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("friends.id", "friends.friendsSince")
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Успех - удаление пользователя из друзей")
    void whenDeleteFriendshipThenReturn204AndRemoveFromDb() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        createFriendshipBetween(fromUserId, toUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", fromUserId.toString());
        var entity = new HttpEntity<>(headers);

        var responseDelete = restTemplate.exchange(
                "/api/friends/{friendId}",
                HttpMethod.DELETE,
                entity,
                Void.class,
                toUserId
        );

        assertThat(responseDelete.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(responseDelete.getBody()).isNull();

        var count = dbAssert.queryForObject("SELECT COUNT(*) FROM friendships", Integer.class);
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Ошибка 404 - попытка удалить пользователя, которого нет в друзьях")
    void whenDeleteNonExistentFriendThenReturn404() {
        var userId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var randomFriendId = UUID.randomUUID();

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/friends/{friendId}",
                HttpMethod.DELETE,
                entity,
                DeleteFriend404Response.class,
                randomFriendId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка 400 - передан невалидный UUID при удалении друга")
    void whenDeleteFriendWithInvalidUuidThenReturn400() {
        var userId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/friends/{friendId}",
                HttpMethod.DELETE,
                entity,
                FindIncomingOfferFriendships400Response.class,
                "invalid-uuid-format"
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Ошибка 400 - некорректные параметры пагинации при поиске друзей")
    void whenFindFriendshipsWithInvalidPaginationThenReturn400() {
        var userId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");

        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/friends/friends?page=0&limit=101",
                HttpMethod.GET,
                entity,
                FindIncomingOfferFriendships400Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void createFriendshipBetween(UUID fromUserId, UUID toUserId) {
        var request = new CreateOfferFriendshipRequest()
                .fromUserId(fromUserId)
                .toUserId(toUserId);

        var responseCreate = restTemplate.postForEntity(
                "/api/friend-requests/create",
                request,
                CreateOfferFriendshipResponse.class
        );
        assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseCreate.getBody()).isNotNull();

        var acceptRequest = new AcceptOfferFriendshipRequest()
                .requestId(responseCreate.getBody().getId());
        var responseAccept = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                acceptRequest,
                AcceptOfferFriendshipResponse.class
        );
        assertThat(responseAccept.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}