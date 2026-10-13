package ru.job4j.socialmediaapi.repository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.job4j.socialmediaapi.exception.*;

@Repository
public class JdbcFriendshipRepository implements FriendshipRepository {

    private static final String CREATE = """
            INSERT INTO friendships (
                id, first_user_id, second_user_id,
                offer_friendship_id, created_at
            )
            VALUES (?, ?, ?, ?, ?)
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

    private final JdbcTemplate jdbcTemplate;

    public JdbcFriendshipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreateFriendshipResponse createFriendship(CreateFriendshipRequest requestCreate)
            throws DomainException {
        try {
            return jdbcTemplate.queryForObject(
                    CREATE,
                    (resultSet, rowNum) -> new CreateFriendshipResponse(
                            resultSet.getObject("id", java.util.UUID.class),
                            resultSet.getObject("first_user_id", java.util.UUID.class),
                            resultSet.getObject("second_user_id", java.util.UUID.class),
                            resultSet.getObject("offer_friendship_id", java.util.UUID.class),
                            resultSet.getTimestamp("created_at").toInstant()
                    ),
                    requestCreate.id(),
                    requestCreate.firstUserId(),
                    requestCreate.secondUserId(),
                    requestCreate.offerFriendshipId(),
                    java.sql.Timestamp.from(requestCreate.createdAt())
            );
        } catch (DataIntegrityViolationException ex) {
            throw handleFriendshipDataIntegrityException(ex);
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

    private DomainException handleFriendshipDataIntegrityException(
            DataIntegrityViolationException ex
    ) {
        if (ex.getCause() instanceof java.sql.SQLException sqlException) {
            String message = sqlException.getMessage();

            if (message != null) {
                if (message.contains("chk_canonical_order")) {
                    return new DatabaseWriteException(
                            "Critical data ordering constraint violated. Check service sorting layer."
                    );
                }
                if (message.contains("uq_friendships_pair")) {
                    return new DuplicateFriendshipRequestException(
                            "Friendship already exists between these users"
                    );
                }
            }
        }
        return new DatabaseWriteException("Unexpected data integrity violation during friendship creation", ex);
    }
}
