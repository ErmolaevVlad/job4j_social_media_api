package ru.job4j.socialmediaapi.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.exception.DuplicateFriendshipRequestException;
import ru.job4j.socialmediaapi.exception.OfferFriendshipNotFoundException;
import ru.job4j.socialmediaapi.exception.SelfFriendshipException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@Transactional
@SpringBootTest
class JdbcOfferFriendshipRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private OfferFriendshipRepository repository;

    @Test
    @DisplayName("Успех — создание новой заявки на дружбу, если её ещё не существует")
    void whenCreateOfferFriendshipThenReturnStoredData() throws DomainException {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");
        var request = new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                fromUserId,
                toUserId,
                FriendshipStatus.PENDING,
                now,
                now
        );

        var result = repository.createOfferFriendship(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(request);
    }

    @Test
    @DisplayName("Ошибка — создание дубликата заявки между теми же пользователями")
    void whenCreateDuplicateOfferFriendshipThenThrowException() throws DomainException {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var status = FriendshipStatus.PENDING;
        var now = Instant.parse("2026-09-22T10:15:00Z");
        repository.createOfferFriendship(request(
                fromUserId, toUserId, status, now
        ));

        assertThatThrownBy(() -> repository.createOfferFriendship(request(
               fromUserId, toUserId, status, now
        ))).isInstanceOf(DuplicateFriendshipRequestException.class);
    }

    @Test
    @DisplayName("Ошибка — попытка отправить заявку на дружбу самому себе")
    void whenCreateOfferFriendshipToSelfThenThrowException() {
        var userId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");
        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                userId, userId, FriendshipStatus.PENDING, now
        ))).isInstanceOf(SelfFriendshipException.class);
    }

    @Test
    @DisplayName("Успех — получение списка входящих заявок пользователя")
    void whenFindIncomingOfferFriendshipsThenReturnUserIncomingRequests() throws DomainException {
        var myId = UUID.randomUUID();
        var userId1 = UUID.randomUUID();
        var userId2 = UUID.randomUUID();
        var status = FriendshipStatus.PENDING;
        var now = Instant.parse("2026-09-22T10:15:00Z");

        repository.createOfferFriendship(request(
                userId1, myId, status, now
        ));
        repository.createOfferFriendship(request(
                userId2, myId, status, now
        ));
        repository.createOfferFriendship(request(
                myId, UUID.randomUUID(), status, now
        ));
        var request = new OfferFriendshipRepository.FindOfferFriendshipsRequest(myId);
        var rsl = repository.findIncomingOfferFriendships(request);
        assertThat(rsl.offers())
                .hasSize(2)
                .extracting("userId")
                .containsExactlyInAnyOrder(userId1, userId2);
    }

    @Test
    @DisplayName("Успех — получение списка исходящих заявок пользователя")
    void whenFindOutgoingOfferFriendshipsThenReturnUserOutgoingRequests() throws DomainException {
        var myId = UUID.randomUUID();
        var userId1 = UUID.randomUUID();
        var userId2 = UUID.randomUUID();
        var status = FriendshipStatus.PENDING;
        var now = Instant.parse("2026-09-22T10:15:00Z");

        repository.createOfferFriendship(request(
                myId, userId1, status, now
        ));
        repository.createOfferFriendship(request(
                myId, userId2, status, now
        ));
        repository.createOfferFriendship(request(
                UUID.randomUUID(), myId, status, now
        ));

        var request = new OfferFriendshipRepository.FindOfferFriendshipsRequest(myId);
        var result = repository.findOutgoingOfferFriendships(request);

        assertThat(result).isNotNull();
        assertThat(result.offers())
                .hasSize(2)
                .extracting("userId")
                .containsExactlyInAnyOrder(userId1, userId2);
    }

    @Test
    @DisplayName("Успех — если у пользователя нет заявок, возвращаются пустые списки")
    void whenUserHasNoRequestsThenReturnEmptyLists() throws DomainException {
        var userId = UUID.randomUUID();
        var request = new OfferFriendshipRepository.FindOfferFriendshipsRequest(userId);

        var incomingResult = repository.findIncomingOfferFriendships(request);
        var outgoingResult = repository.findOutgoingOfferFriendships(request);

        assertThat(incomingResult.offers()).isEmpty();
        assertThat(outgoingResult.offers()).isEmpty();
    }

    @Test
    @DisplayName("Успех — поиск заявки по существующему ID")
    void whenFindByIdExistingThenReturnCorrectData() throws DomainException {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");

        var response = repository.createOfferFriendship(request(
                fromUserId, toUserId, FriendshipStatus.PENDING, now
        ));

        var request = new OfferFriendshipRepository.FindByIdRequest(response.id());
        var result = repository.findById(request);

        assertThat(result).isNotNull();
        assertThat(result.OfferFriendshipId()).isEqualTo(response.id());
        assertThat(result.fromUserId()).isEqualTo(fromUserId);
        assertThat(result.toUserId()).isEqualTo(toUserId);
    }

    @Test
    @DisplayName("Ошибка — поиск заявки по несуществующему ID должен выбросить OfferFriendshipNotFoundException")
    void whenFindByIdNonExistingThenThrowDatabaseReadException() {
        var nonExistingId = UUID.randomUUID();
        var request = new OfferFriendshipRepository.FindByIdRequest(nonExistingId);
        assertThatThrownBy(() -> repository.findById(request))
                .isInstanceOf(OfferFriendshipNotFoundException.class)
                .hasMessageContaining("Friendship request not found with ID: " + nonExistingId);
    }

    @Test
    @DisplayName("Успех — попытка обновить статус для несуществующего ID должна вернуть false")
    void whenUpdateStatusNonExistingThenReturnFalse() throws DomainException {
        var nonExistingId = UUID.randomUUID();
        boolean isUpdated = repository.updateStatusOfferFriendship(nonExistingId, FriendshipStatus.ACCEPTED);
        assertThat(isUpdated).isFalse();
    }

    private OfferFriendshipRepository.CreateOfferFriendshipRequest request(
            UUID fromUserId,
            UUID toUserId,
            FriendshipStatus status,
            Instant now
    ) {
        return new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                fromUserId, toUserId, status, now, now
        );
    }
}