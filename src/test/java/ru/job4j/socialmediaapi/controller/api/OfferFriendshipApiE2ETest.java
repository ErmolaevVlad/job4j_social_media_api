package ru.job4j.socialmediaapi.controller.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OfferFriendshipApiE2ETest {
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
    @DisplayName("Успех - создание и сохрание заявки в друзья")
    void whenCreateOfferFriendshipThenOfferIsSaved() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var request = new CreateOfferFriendshipRequest()
                .fromUserId(fromUserId)
                .toUserId(toUserId);

        var response = restTemplate.postForEntity(
                "/api/friend-requests/create",
                request,
                CreateOfferFriendshipResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var responseDto = response.getBody();
        var expected = new CreateOfferFriendshipResponse()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .status(CreateOfferFriendshipResponse.StatusEnum.PENDING);

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(expected);

        var dbStatus = dbAssert.queryForObject(
                "Select status FROM offer_friendships WHERE id = ?",
                String.class,
                responseDto.getId()
        );
        assertThat(dbStatus).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Успех - принятие заявки в друзья, статус меняется на ACCEPTED")
    void whenAcceptOfferFriendshipThenStatusAccepted() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var idOfferFriendship = createOfferFriendshipAndGetId(fromUserId, toUserId);
        var acceptRequest = new AcceptOfferFriendshipRequest()
                .requestId(idOfferFriendship);
        var response = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                acceptRequest,
                AcceptOfferFriendshipResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo(AcceptOfferFriendshipResponse.StatusEnum.ACCEPTED);

        var expectedDto = new AcceptOfferFriendshipResponse()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .status(AcceptOfferFriendshipResponse.StatusEnum.ACCEPTED);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedDto);

        var dbStatus = dbAssert.queryForObject(
                "Select status FROM offer_friendships WHERE id = ?",
                String.class,
                idOfferFriendship
        );
        assertThat(dbStatus).isEqualTo("ACCEPTED");

        var dbFriendship = dbAssert.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE id = ?",
                Integer.class,
                response.getBody().getId()
        );
        assertThat(dbFriendship).isEqualTo(1);
    }

    @Test
    @DisplayName("Успех - отклонение заявки в друзья, статус меняется на REJECTED")
    void whenRejectOfferFriendshipThenStatusRejected() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var idOfferFriendship = createOfferFriendshipAndGetId(fromUserId, toUserId);
        var rejectRequest = new RejectOfferFriendshipsRequest()
                .requestId(idOfferFriendship);
        var response = restTemplate.postForEntity(
                "/api/friend-requests/reject",
                rejectRequest,
                RejectOfferFriendshipsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert response.getBody() != null;
        assertThat(response.getBody().getStatus()).isEqualTo(RejectOfferFriendshipsResponse.StatusEnum.REJECTED);

        var expectedDto = new RejectOfferFriendshipsResponse()
                .id(idOfferFriendship)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .status(RejectOfferFriendshipsResponse.StatusEnum.REJECTED);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto);

        var dbStatus = dbAssert.queryForObject(
                "Select status FROM offer_friendships WHERE id = ?",
                String.class,
                idOfferFriendship
        );
        assertThat(dbStatus).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("Успех - получение списка входящих заявок в друзья")
    void whenFindIncomingOfferFriendshipsThenReturnList() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var offerId = createOfferFriendshipAndGetId(fromUserId, toUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", toUserId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/friend-requests/incoming?page=1&limit=20",
                HttpMethod.GET,
                entity,
                FindIncomingOfferFriendshipsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedItem = new FindIncomingOfferFriendshipsResponseRequestsInner()
                .id(offerId)
                .username(fromUserId.toString());

        var expectedMeta = new FindIncomingOfferFriendshipsResponseMeta()
                .currentPage(1)
                .totalItems(1L)
                .totalPages(1);

        var expectedDto = new FindIncomingOfferFriendshipsResponse()
                .requests(java.util.List.of(expectedItem))
                .meta(expectedMeta);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("requests.id", "requests.createdAt") // Игнорируем timestamp внутри списка
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Успех - получение списка исходящих заявок в друзья")
    void whenFindOutgoingOfferFriendshipsThenReturnList() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var offerId = createOfferFriendshipAndGetId(fromUserId, toUserId);

        var headers = new HttpHeaders();
        headers.set("X-User-Id", fromUserId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/friend-requests/outgoing?page=1&limit=20",
                HttpMethod.GET,
                entity,
                FindOutgoingOfferFriendshipsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        var expectedItem = new FindOutgoingOfferFriendshipsResponseRequestsInner()
                .id(offerId)
                .username(toUserId.toString());

        var expectedMeta = new FindOutgoingOfferFriendshipsResponseMeta()
                .currentPage(1)
                .totalItems(1L)
                .totalPages(1);

        var expectedDto = new FindOutgoingOfferFriendshipsResponse()
                .requests(java.util.List.of(expectedItem))
                .meta(expectedMeta);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("requests.id", "requests.createdAt")
                .isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Ошибка 409 - попытка создать заявку самому себе")
    void whenCreateOfferToMyselfThenReturn409() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");

        var request = new CreateOfferFriendshipRequest()
                .fromUserId(fromUserId)
                .toUserId(fromUserId);

        var response = restTemplate.postForEntity(
                "/api/friend-requests/create",
                request,
                CreateOfferFriendship409Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Ошибка 404 - принятие несуществующей заявки")
    void whenAcceptNonExistentOfferThenReturn404() {
        var acceptRequest = new AcceptOfferFriendshipRequest()
                .requestId(UUID.randomUUID());

        var response = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                acceptRequest,
                AcceptOfferFriendship404Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка 409 - принятие уже отклоненной заявки")
    void whenAcceptAlreadyRejectedOfferThenReturn409() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var idOfferFriendship = createOfferFriendshipAndGetId(fromUserId, toUserId);

        var rejectRequest = new RejectOfferFriendshipsRequest().requestId(idOfferFriendship);
        restTemplate.postForEntity(
                "/api/friend-requests/reject",
                rejectRequest,
                RejectOfferFriendshipsResponse.class);

        var acceptRequest = new AcceptOfferFriendshipRequest().requestId(idOfferFriendship);
        var response = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                acceptRequest,
                AcceptOfferFriendship409Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Ошибка 204 - заявка уже существует")
    void whenOfferAlreadyCreatedThenReturn204() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        createOfferFriendshipAndGetId(fromUserId, toUserId);

        var duplicateRequest = new CreateOfferFriendshipRequest()
                .fromUserId(fromUserId)
                .toUserId(toUserId);
        var response = restTemplate.postForEntity(
                "/api/friend-requests/create",
                duplicateRequest,
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @DisplayName("Статус 204 - принятие встречной заявки, когда дружба уже создана")
    void whenAcceptMutualOfferAndFriendshipExistsThenReturn204() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");

        var offerAtoB = createOfferFriendshipAndGetId(fromUserId, toUserId);
        var offerBtoA = createOfferFriendshipAndGetId(toUserId, fromUserId);

        var firstAccept = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                new AcceptOfferFriendshipRequest().requestId(offerAtoB),
                AcceptOfferFriendshipResponse.class
        );
        assertThat(firstAccept.getStatusCode()).isEqualTo(HttpStatus.OK);

        var secondAccept = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                new AcceptOfferFriendshipRequest().requestId(offerBtoA),
                Void.class
        );
        assertThat(secondAccept.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(secondAccept.getBody()).isNull();

        var secondOfferStatus = dbAssert.queryForObject(
                "SELECT status FROM offer_friendships WHERE id = ?",
                String.class,
                offerBtoA
        );
        assertThat(secondOfferStatus).isEqualTo("ACCEPTED");

        var friendshipsCount = dbAssert.queryForObject(
                "SELECT COUNT(*) FROM friendships",
                Integer.class
        );
        assertThat(friendshipsCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Ошибка 409 - попытка отклонить уже принятую заявку")
    void whenRejectAlreadyAcceptedOfferThenReturn409() {
        var fromUserId = UUID.fromString("77926b15-870c-4366-93e3-6ac74b61b894");
        var toUserId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var idOfferFriendship = createOfferFriendshipAndGetId(fromUserId, toUserId);

        restTemplate.postForEntity(
                "/api/friend-requests/accept",
                new AcceptOfferFriendshipRequest().requestId(idOfferFriendship),
                AcceptOfferFriendshipResponse.class
        );

        var response = restTemplate.postForEntity(
                "/api/friend-requests/reject",
                new RejectOfferFriendshipsRequest().requestId(idOfferFriendship),
                RejectOfferFriendships409Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        var dbStatus = dbAssert.queryForObject(
                "SELECT status FROM offer_friendships WHERE id = ?",
                String.class,
                idOfferFriendship
        );
        assertThat(dbStatus).isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("Ошибка 404 - отклонение несуществующей заявки")
    void whenRejectNonExistentOfferThenReturn404() {
        var rejectRequest = new RejectOfferFriendshipsRequest()
                .requestId(UUID.randomUUID());

        var response = restTemplate.postForEntity(
                "/api/friend-requests/reject",
                rejectRequest,
                RejectOfferFriendships404Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка 400 - валидация при принятии заявки с пустым requestId")
    void whenAcceptOfferWithNullIdThenReturn400() {
        var invalidRequest = new AcceptOfferFriendshipRequest()
                .requestId(null);

        var response = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                invalidRequest,
                AcceptOfferFriendship400Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Ошибка 400 - валидация некорректных параметров пагинации")
    void whenFindIncomingWithInvalidPaginationThenReturn400() {
        var userId = UUID.fromString("fd9e3b80-9602-427f-87ec-5763c4afbcd8");
        var headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        var entity = new HttpEntity<>(headers);

        var response = restTemplate.exchange(
                "/api/friend-requests/incoming?page=0&limit=101",
                HttpMethod.GET,
                entity,
                FindIncomingOfferFriendships400Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private UUID createOfferFriendshipAndGetId(UUID fromUserId, UUID toUserId) {
        var request = new CreateOfferFriendshipRequest()
                .fromUserId(fromUserId)
                .toUserId(toUserId);

        var response = restTemplate.postForEntity(
                "/api/friend-requests/create",
                request,
                CreateOfferFriendshipResponse.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().getId();
    }
}