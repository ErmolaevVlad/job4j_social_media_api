package ru.job4j.socialmediaapi.controller.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.socialmediaapi.controller.dto.*;
import ru.job4j.socialmediaapi.repository.FriendshipStatus;
import ru.job4j.socialmediaapi.repository.OfferFriendshipRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestRestTemplate
class OfferFriendshipApiE2ETest extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    OfferFriendshipRepository repository;

    @Test
    @DisplayName("Успех - создание и сохрание заявки в друзья")
    void whenCreateOfferFriendshipThenOfferIsSaved() {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var request = new CreateOfferFriendshipRequest()
                .fromUserId(fromUserId)
                .toUserId(toUserId);

        var response = restTemplate.postForEntity(
                "/api/friend-requests/create",
                request,
                CreateOfferFriendshipResponse.class
        );
        assertThat(response.getBody()).isNotNull();
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

        var dbOffer = repository.findById(new OfferFriendshipRepository.FindByIdRequest(
                response.getBody().getId()
        ));
        assertThat(dbOffer.status()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    @DisplayName("Успех - принятие заявки в друзья, статус меняется на ACCEPTED")
    void whenAcceptOfferFriendshipThenStatusAccepted() {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();

        var offerFriendship = createOfferFriendship(fromUserId, toUserId);

        var acceptRequest = new AcceptOfferFriendshipRequest()
                .requestId(offerFriendship.id());
        var response = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                acceptRequest,
                AcceptOfferFriendshipResponse.class,
                String.class
        );
        System.out.println("ОТВЕТ СЕРВЕРА: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(AcceptOfferFriendshipResponse.StatusEnum.ACCEPTED);

        boolean isUser1First = fromUserId.toString().compareTo(toUserId.toString()) < 0;
        UUID firstUserId = isUser1First ? fromUserId : toUserId;
        UUID secondUserId = isUser1First ? toUserId : fromUserId;

        var expectedDto = new AcceptOfferFriendshipResponse()
                .fromUserId(firstUserId)
                .toUserId(secondUserId)
                .status(AcceptOfferFriendshipResponse.StatusEnum.ACCEPTED);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedDto);

        var dbOffer = repository.findById(new OfferFriendshipRepository.FindByIdRequest(
                offerFriendship.id()
        ));
        assertThat(dbOffer.status()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Успех - отклонение заявки в друзья, статус меняется на REJECTED")
    void whenRejectOfferFriendshipThenStatusRejected() {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var offerFriendship = createOfferFriendship(fromUserId, toUserId);

        var rejectRequest = new RejectOfferFriendshipsRequest()
                .requestId(offerFriendship.id());
        var response = restTemplate.postForEntity(
                "/api/friend-requests/reject",
                rejectRequest,
                RejectOfferFriendshipsResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(RejectOfferFriendshipsResponse.StatusEnum.REJECTED);

        var expectedDto = new RejectOfferFriendshipsResponse()
                .id(offerFriendship.id())
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .status(RejectOfferFriendshipsResponse.StatusEnum.REJECTED);

        assertThat(response.getBody())
                .usingRecursiveComparison()
                .isEqualTo(expectedDto);

        var dbOffer = repository.findById(new OfferFriendshipRepository.FindByIdRequest(
                offerFriendship.id()
        ));
        assertThat(dbOffer.status()).isEqualTo(FriendshipStatus.REJECTED);
    }

    @Test
    @DisplayName("Успех - получение списка входящих заявок в друзья")
    void whenFindIncomingOfferFriendshipsThenReturnList() {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var offerFriendship = createOfferFriendship(fromUserId, toUserId);

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
                .id(offerFriendship.id())
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
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var offerFriendship = createOfferFriendship(fromUserId, toUserId);

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
                .id(offerFriendship.id())
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
        var fromUserId = UUID.randomUUID();

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
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var offerFriendship = createOfferFriendship(fromUserId, toUserId);

        var rejectRequest = new RejectOfferFriendshipsRequest().requestId(offerFriendship.id());
        restTemplate.postForEntity(
                "/api/friend-requests/reject",
                rejectRequest,
                RejectOfferFriendshipsResponse.class);

        var acceptRequest = new AcceptOfferFriendshipRequest().requestId(offerFriendship.id());
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
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        createOfferFriendship(fromUserId, toUserId);

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
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();

        var offerAtoB = createOfferFriendship(fromUserId, toUserId);
        var offerBtoA = createOfferFriendship(toUserId, fromUserId);

        var firstAccept = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                new AcceptOfferFriendshipRequest().requestId(offerAtoB.id()),
                AcceptOfferFriendshipResponse.class
        );
        assertThat(firstAccept.getStatusCode()).isEqualTo(HttpStatus.OK);

        var secondAccept = restTemplate.postForEntity(
                "/api/friend-requests/accept",
                new AcceptOfferFriendshipRequest().requestId(offerBtoA.id()),
                Void.class
        );
        assertThat(secondAccept.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(secondAccept.getBody()).isNull();

        var dbOffer = repository.findById(new OfferFriendshipRepository.FindByIdRequest(
                offerBtoA.id()
        ));
        assertThat(dbOffer.status()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    @DisplayName("Ошибка 409 - попытка отклонить уже принятую заявку")
    void whenRejectAlreadyAcceptedOfferThenReturn409() {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var offerFriendship = createOfferFriendship(fromUserId, toUserId);

        restTemplate.postForEntity(
                "/api/friend-requests/accept",
                new AcceptOfferFriendshipRequest().requestId(offerFriendship.id()),
                AcceptOfferFriendshipResponse.class
        );

        var response = restTemplate.postForEntity(
                "/api/friend-requests/reject",
                new RejectOfferFriendshipsRequest().requestId(offerFriendship.id()),
                RejectOfferFriendships409Response.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        var dbOffer = repository.findById(new OfferFriendshipRepository.FindByIdRequest(
                offerFriendship.id()
        ));
        assertThat(dbOffer.status()).isEqualTo(FriendshipStatus.ACCEPTED);
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

    private OfferFriendshipRepository.CreateOfferFriendshipResponse createOfferFriendship(UUID fromUserId, UUID toUserId) {

        OfferFriendshipRepository.CreateOfferFriendshipRequest request = new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                fromUserId,
                toUserId,
                FriendshipStatus.PENDING,
                Instant.now(),
                Instant.now()
        );
        return repository.createOfferFriendship(request);
    }
}