package ru.job4j.socialmediaapi.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
    void whenCreateOfferFriendshipThenReturnStoredData() {
        var id = UUID.randomUUID();
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        var now = Instant.parse("2026-09-22T10:15:00Z");
        var request = new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                id,
                fromUserId,
                toUserId,
                "PENDING",
                now,
                now
        );

        var result = repository.createOfferFriendship(request);

        var expected = new OfferFriendshipRepository
                .CreateOfferFriendshipResponse(
                id,
                fromUserId,
                toUserId,
                "PENDING",
                now,
                now
        );
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void whenCreateDuplicateOfferFriendshipThenThrowException() {
        var fromUserId = UUID.randomUUID();
        var toUserId = UUID.randomUUID();
        repository.createOfferFriendship(request(
                UUID.randomUUID(), fromUserId, toUserId, "PENDING"
        ));

        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                UUID.randomUUID(), fromUserId, toUserId, "PENDING"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void whenCreateOfferFriendshipToSelfThenThrowException() {
        var userId = UUID.randomUUID();

        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                UUID.randomUUID(), userId, userId, "PENDING"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void whenCreateOfferFriendshipWithUnknownStatusThenThrowException() {
        assertThatThrownBy(() -> repository.createOfferFriendship(request(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "UNKNOWN"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    private OfferFriendshipRepository.CreateOfferFriendshipRequest request(
            UUID id,
            UUID fromUserId,
            UUID toUserId,
            String status
    ) {
        var now = Instant.parse("2026-09-22T10:15:00Z");
        return new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                id, fromUserId, toUserId, status, now, now
        );
    }
}