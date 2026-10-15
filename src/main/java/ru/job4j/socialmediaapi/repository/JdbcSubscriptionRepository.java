package ru.job4j.socialmediaapi.repository;

import org.postgresql.util.PSQLException;
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

    private static final String DELETE = """
            DELETE FROM subscriptions
            WHERE follower_id = ? AND followed_id = ?
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
            if (ex.getMostSpecificCause() instanceof PSQLException pgException) {
                var serverError = pgException.getServerErrorMessage();
                if (serverError != null) {
                    var constraint = serverError.getConstraint();
                    if ("uq_follower_followed".equals(constraint)) {
                        throw new DuplicateSubscriptionException(
                                "User " + requestCreate.follower_id()
                                        + " is already subscribed to "
                                        + requestCreate.followed_id());
                    }
                    if ("chk_not_self_subscriptions".equals(constraint)) {
                        throw new SelfSubscriptionException(
                                "User " + requestCreate.follower_id() + " cannot send a friendship offer to themselves"
                        );
                    }
                }
            }
            throw new DatabaseWriteException("Unexpected data integrity violation during subscription creation", ex);
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

    @Override
    public void deleteSubscription(DeleteSubscriptionRequest request) throws DomainException {
        try {
            int rowsAffected = jdbcTemplate.update(
                    DELETE,
                    request.follower_id(),
                    request.followed_id()
            );

            if (rowsAffected == 0) {
                throw new SubscriptionNotFoundException(
                        "Subscription not found: user " + request.follower_id()
                                + " is not subscribed to " + request.followed_id()
                );
            }
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new DatabaseWriteException(
                    "Failed to delete subscription for follower: " + request.follower_id(), ex
            );
        }
    }
}
