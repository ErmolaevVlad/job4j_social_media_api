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
import ru.job4j.socialmediaapi.exception.DatabaseWriteException;
import ru.job4j.socialmediaapi.exception.DomainException;
import ru.job4j.socialmediaapi.exception.DuplicateFriendshipRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Testcontainers
@Transactional
@SpringBootTest
class JdbcFriendshipRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private OfferFriendshipRepository offerFriendshipRepository;
    @Autowired
    private FriendshipRepository friendshipRepository;

    private static final UUID USER_1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant FIXED_NOW = Instant.parse("2026-09-22T10:15:00Z");

    @Test
    @DisplayName("Успех - создание дружбы с автогенерацией UUID")
    void whenCreateFriendshipThenReturnStoredData() throws DomainException {
        var offerResponse = createOffer(USER_1, USER_2);

        var friendshipRequest = new FriendshipRepository.CreateFriendshipRequest(
                USER_1,
                USER_2,
                offerResponse.id(),
                FIXED_NOW
        );

        var rsl = friendshipRepository.createFriendship(friendshipRequest);

        assertThat(rsl).isNotNull();
        assertThat(rsl.id()).isNotNull();

        assertThat(rsl)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(friendshipRequest);
    }

    @Test
    @DisplayName("Успех - вернуть список друзей")
    void whenFindFriendshipThenReturnFriends() throws DomainException {
        createFriendshipPair(USER_1, USER_2);

        var rsl = friendshipRepository.findFriendships(
                new FriendshipRepository.FindFriendshipsRequest(USER_1)
        );

        var expected = new FriendshipRepository.FindFriendshipsResponse(
                List.of(new FriendshipRepository.FriendshipInfo(USER_2, FIXED_NOW))
        );

        assertThat(rsl).isEqualTo(expected);
    }

    @Test
    @DisplayName("Должен выбросить исключение при нарушении chk_canonical_order (first_user_id >= second_user_id)")
    void shouldThrowExceptionWhenCanonicalOrderIsViolated() throws DomainException {
        var offerResponse = createOffer(USER_2, USER_1);

        var friendshipRequest = new FriendshipRepository.CreateFriendshipRequest(
                USER_2,
                USER_1,
                offerResponse.id(),
                FIXED_NOW
        );

        assertThatThrownBy(() -> friendshipRepository.createFriendship(friendshipRequest))
                .isInstanceOf(DatabaseWriteException.class);
    }

    @Test
    @DisplayName("Должен выбросить DuplicateFriendshipRequestException при попытке повторно добавить тех же пользователей")
    void shouldThrowDuplicateFriendshipExceptionWhenPairAlreadyExists() throws DomainException {

        var createOfferResponse = createOffer(USER_1, USER_2);

        var firstFriendshipRequest = new FriendshipRepository.CreateFriendshipRequest(
                USER_1, USER_2, createOfferResponse.id(), FIXED_NOW
        );
        friendshipRepository.createFriendship(firstFriendshipRequest);
        UUID secondOfferFriendshipId = UUID.randomUUID();

        var secondFriendshipRequest = new FriendshipRepository.CreateFriendshipRequest(
                USER_1,
                USER_2,
                secondOfferFriendshipId,
                FIXED_NOW
        );

        assertThatThrownBy(() -> friendshipRepository.createFriendship(secondFriendshipRequest))
                .isInstanceOf(DuplicateFriendshipRequestException.class);
    }

    private OfferFriendshipRepository.CreateOfferFriendshipResponse createOffer(UUID fromUser, UUID toUser) throws DomainException {
        var offerRequest = new OfferFriendshipRepository.CreateOfferFriendshipRequest(
                fromUser, toUser, FriendshipStatus.PENDING, FIXED_NOW, FIXED_NOW
        );
        return offerFriendshipRepository.createOfferFriendship(offerRequest);
    }

    private void createFriendshipPair(UUID fromUser, UUID toUser) throws DomainException {
        var createOfferResponse = createOffer(fromUser, toUser);
        var friendshipRequest = new FriendshipRepository.CreateFriendshipRequest(
                fromUser, toUser, createOfferResponse.id(), FIXED_NOW
        );
        friendshipRepository.createFriendship(friendshipRequest);
    }
}