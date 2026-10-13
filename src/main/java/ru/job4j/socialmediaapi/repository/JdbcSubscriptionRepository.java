package ru.job4j.socialmediaapi.repository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.job4j.socialmediaapi.exception.*;

@Repository
public class JdbcSubscriptionRepository implements SubscriptionRepository {

    private static final String CREATE = """
            INSERT INTO subscriptions (
                id, followed_id, follower_id,
                created_at
            )
            VALUES (?, ?, ?, ?)
            RETURNING id, followed_id, follower_id,
                      created_at
            """;

    private static final String SELECT_FOLLOWERS = """
            SELECT follower_id,
                   created_at
            FROM subscriptions
            WHERE followed_id = ?
            ORDER BY created_at DESC, id DESC
            """;

    private static final String SELECT_SUBSCRIPTIONS = """
            SELECT followed_id,
                   created_at
            FROM subscriptions
            WHERE follower_id = ?
            ORDER BY created_at DESC, id DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcSubscriptionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreateFollowerResponse createFollower(CreateFollowerRequest requestCreate)
            throws DomainException {
        try {
            return jdbcTemplate.queryForObject(
                    CREATE,
                    (resultSet, rowNum) -> new CreateFollowerResponse(
                            resultSet.getObject("id", java.util.UUID.class),
                            resultSet.getObject("follower_id", java.util.UUID.class),
                            resultSet.getObject("followed_id", java.util.UUID.class),
                            resultSet.getTimestamp("created_at").toInstant()
                    ),
                    requestCreate.id(),
                    requestCreate.follower_id(),
                    requestCreate.followed_id(),
                    java.sql.Timestamp.from(requestCreate.createdAt())
            );
        } catch (DataIntegrityViolationException ex) {
            throw handleSubscriptionDataIntegrityException(ex, requestCreate);
        }
    }

    @Override
    public FindFollowersResponse findFollowers(FindUserRequest request)
            throws DomainException {
        try {
            return new FindFollowersResponse(
                    jdbcTemplate.query(
                            SELECT_FOLLOWERS,
                            (resultSet, rowNum) -> new UserInfo(
                                    resultSet.getObject("follower_id", java.util.UUID.class),
                                    resultSet.getTimestamp("created_at").toInstant()
                            ),
                            request.userId()
                    )
            );
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new DatabaseReadException("Failed to fetch follower for user: " + request.userId(), ex);
        }
    }

    @Override
    public FindSubscriptionsResponse findSubscriptions(FindUserRequest request) throws DomainException {
        try {
            return new FindSubscriptionsResponse(
                    jdbcTemplate.query(
                            SELECT_SUBSCRIPTIONS,
                            (resultSet, rowNum) -> new UserInfo(
                                    resultSet.getObject("followed_id", java.util.UUID.class),
                                    resultSet.getTimestamp("created_at").toInstant()
                            ),
                            request.userId()
                    )
            );
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new DatabaseReadException("Failed to fetch subscriptions for user: " + request.userId(), ex);
        }
    }

    private DomainException handleSubscriptionDataIntegrityException(
            DataIntegrityViolationException ex,
            CreateFollowerRequest  request
    ) {
        if (ex.getCause() instanceof java.sql.SQLException sqlException) {
            String message = sqlException.getMessage();

            if (message != null) {
                if (message.contains("uq_follower_followed")) {
                    return new DuplicateSubscriptionException(
                            "User " + request.follower_id() + " is already subscribed to " + request.followed_id()
                    );
                }
                if (message.contains("chk_not_self_subscriptions")) {
                    return new SelfSubscriptionException(
                            "User " + request.follower_id() + " cannot send a friendship offer to themselves"
                    );
                }
            }
        }
        return new DatabaseWriteException("Unexpected data integrity violation during subscription creation", ex);
    }
}
