package ru.job4j.socialmediaapi.repository;

import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.job4j.socialmediaapi.exception.*;

import java.util.UUID;

@Repository
public class JdbcFriendshipRepository implements FriendshipRepository {

    private static final String CREATE = """
            INSERT INTO friendships (
                id, first_user_id, second_user_id,
                offer_friendship_id, created_at
            )
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (first_user_id, second_user_id) DO NOTHING
            RETURNING id, first_user_id, second_user_id,
                      offer_friendship_id, created_at
            """;

    private static final String SELECT_FRIENDS = """
        SELECT CASE
                   WHEN first_user_id = ? THEN second_user_id
                   ELSE first_user_id
               END AS friend_id,
               created_at
        FROM friendships
        WHERE first_user_id = ? OR second_user_id = ?
        ORDER BY created_at DESC, id DESC
        """;

    private static final String DELETE_FRIENDSHIP = """
            DELETE FROM friendships WHERE first_user_id = ? and second_user_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcFriendshipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreateFriendshipResponse createFriendship(CreateFriendshipRequest requestCreate)
            throws DomainException {
        UUID friendshipId = UUID.randomUUID();
        try {
            var result = jdbcTemplate.query(
                    CREATE,
                    (resultSet, rowNum) -> new CreateFriendshipResponse(
                            resultSet.getObject("id", java.util.UUID.class),
                            resultSet.getObject("first_user_id", java.util.UUID.class),
                            resultSet.getObject("second_user_id", java.util.UUID.class),
                            resultSet.getObject("offer_friendship_id", java.util.UUID.class),
                            resultSet.getTimestamp("created_at").toInstant()
                    ),
                    friendshipId,
                    requestCreate.firstUserId(),
                    requestCreate.secondUserId(),
                    requestCreate.offerFriendshipId(),
                    java.sql.Timestamp.from(requestCreate.createdAt())
            );

            if (result.isEmpty()) {
                throw new DuplicateFriendshipException("Friendship already exists between these users");
            }

            return result.getFirst();
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMostSpecificCause() instanceof PSQLException pgException) {
                var serverError = pgException.getServerErrorMessage();
                if (serverError != null && "chk_canonical_order".equals(serverError.getConstraint())) {
                    throw new DatabaseWriteException(
                            "Critical data ordering constraint violated. Check service sorting layer.", ex);
                }
            }
            throw new DatabaseWriteException("Unexpected data integrity violation during friendship creation", ex);
        }
    }

    @Override
    public FindFriendshipsResponse findFriendships(FindFriendshipsRequest request) throws DomainException {
        try {
            return new FindFriendshipsResponse(
                    jdbcTemplate.query(
                            SELECT_FRIENDS,
                            (resultSet, rowNum) -> new FriendshipInfo(
                                    resultSet.getObject("friend_id", java.util.UUID.class),
                                    resultSet.getTimestamp("created_at").toInstant()
                            ),
                            request.userId(),
                            request.userId(),
                            request.userId()
                    )
            );
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new DatabaseReadException("Failed to fetch subscriptions for user: " + request.userId(), ex);
        }
    }

    @Override
    public void deleteFriendship(DeleteFriendshipRequest request) throws DomainException {
        boolean isFirstSmaller = request.firstUserId().toString()
                .compareTo(request.secondUserId().toString()) < 0;
        UUID firstUserId = isFirstSmaller ? request.firstUserId() : request.secondUserId();
        UUID secondUserId = isFirstSmaller ? request.secondUserId() : request.firstUserId();
        int rowsAffected = jdbcTemplate.update(DELETE_FRIENDSHIP, firstUserId, secondUserId);
        if (rowsAffected != 1) {
            throw new FriendNotFoundException("User not found in the friends list");
        }
    }
}