package ru.job4j.socialmediaapi.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.job4j.socialmediaapi.exception.DuplicateFriendshipRequestException;
import ru.job4j.socialmediaapi.exception.SelfFriendshipException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
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
    void whenCreateOfferFriendshipThenReturnStoredData() {
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
    void whenCreateDuplicateOfferFriendshipThenThrowException() {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");
        repository.createOfferFriendship(request(
                UUID.randomUUID(), fromUserId, toUserId, "PENDING", now
        ));

        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                UUID.randomUUID(), fromUserId, toUserId, "PENDING", now
        ))).isInstanceOf(DuplicateFriendshipRequestException.class);
    }

    @Test
    @DisplayName("Ошибка — попытка отправить заявку на дружбу самому себе")
    void whenCreateOfferFriendshipToSelfThenThrowException() {
        var userId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");
        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                UUID.randomUUID(), userId, userId, "PENDING", now
        ))).isInstanceOf(SelfFriendshipException.class);
    }

    private OfferFriendshipRepository.CreateOfferFriendshipRequest request(
            UUID id,
            UUID fromUserId,
            UUID toUserId,
            String status,
            Instant now
    ) {
        return new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                id, fromUserId, toUserId, FriendshipStatus.valueOf(status), now, now
        );
    }
}