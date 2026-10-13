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
        var id = UUID.randomUUID();
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");
        var request = new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                id,
                fromUserId,
                toUserId,
                FriendshipStatus.PENDING,
                now,
                now
        );

        var result = repository.createOfferFriendship(request);

        var expected = new OfferFriendshipRepository
                .CreateOfferFriendshipResponse(
                id,
                fromUserId,
                toUserId,
                FriendshipStatus.PENDING,
                now,
                now
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("Ошибка — создание дубликата заявки между теми же пользователями")
    void whenCreateDuplicateOfferFriendshipThenThrowException() throws DomainException {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var status = FriendshipStatus.PENDING;
        var now = Instant.parse("2026-09-22T10:15:00Z");
        repository.createOfferFriendship(request(
                UUID.randomUUID(), fromUserId, toUserId, status, now
        ));

        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                UUID.randomUUID(), fromUserId, toUserId, status, now
        ))).isInstanceOf(DuplicateFriendshipRequestException.class);
    }

    @Test
    @DisplayName("Ошибка — попытка отправить заявку на дружбу самому себе")
    void whenCreateOfferFriendshipToSelfThenThrowException() {
        var userId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");
        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                UUID.randomUUID(), userId, userId, FriendshipStatus.PENDING, now
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
                UUID.randomUUID(), userId1, myId, status, now
        ));
        repository.createOfferFriendship(request(
                UUID.randomUUID(), userId2, myId, status, now
        ));
        repository.createOfferFriendship(request(
                UUID.randomUUID(), myId, UUID.randomUUID(), status, now
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
                UUID.randomUUID(), myId, userId1, status, now
        ));
        repository.createOfferFriendship(request(
                UUID.randomUUID(), myId, userId2, status, now
        ));
        repository.createOfferFriendship(request(
                UUID.randomUUID(), UUID.randomUUID(), myId, status, now
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

    private OfferFriendshipRepository.CreateOfferFriendshipRequest request(
            UUID id,
            UUID fromUserId,
            UUID toUserId,
            FriendshipStatus status,
            Instant now
    ) {
        return new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                id, fromUserId, toUserId, status, now, now
        );
    }
}