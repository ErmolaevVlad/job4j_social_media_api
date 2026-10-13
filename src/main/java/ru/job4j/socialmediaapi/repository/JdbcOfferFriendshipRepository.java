package ru.job4j.socialmediaapi.repository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.job4j.socialmediaapi.exception.*;

@Repository
public class JdbcOfferFriendshipRepository implements OfferFriendshipRepository {
    private static final String CREATE = """
            INSERT INTO offer_friendships (
                id, from_user_id, to_user_id,
                status, created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id, from_user_id, to_user_id,
                      status, created_at, updated_at
            """;

    private static final String SELECT_OFFER_INCOMING = """
            SELECT from_user_id,
                   created_at
            FROM offer_friendships
            WHERE to_user_id = ? and status = 'PENDING'
            ORDER BY created_at DESC, id DESC
            """;
    private static final String SELECT_OFFER_OUTGOING = """
            SELECT to_user_id,
                   created_at
            FROM offer_friendships
            WHERE from_user_id = ? AND status = 'PENDING'
            ORDER BY created_at DESC, id DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcOfferFriendshipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreateOfferFriendshipResponse createOfferFriendship(
            CreateOfferFriendshipRequest request
    ) throws DomainException {
        try {
            return jdbcTemplate.queryForObject(
                    CREATE,
                    (resultSet, rowNum) -> new CreateOfferFriendshipResponse(
                            resultSet.getObject("id", java.util.UUID.class),
                            resultSet.getObject("from_user_id", java.util.UUID.class),
                            resultSet.getObject("to_user_id", java.util.UUID.class),
                            FriendshipStatus.valueOf(resultSet.getString("status")),
                            resultSet.getTimestamp("created_at").toInstant(),
                            resultSet.getTimestamp("updated_at").toInstant()
                    ),
                    request.id(),
                    request.fromUserId(),
                    request.toUserId(),
                    request.status().name(),
                    java.sql.Timestamp.from(request.createdAt()),
                    java.sql.Timestamp.from(request.updatedAt())
            );
        } catch (DataIntegrityViolationException ex) {
            throw handleDataIntegrityViolationException(ex, request);
        }
    }

    @Override
    public FindIncomingOfferFriendshipsResponse findIncomingOfferFriendships(FindOfferFriendshipsRequest request)
            throws DomainException {
        try {
            return new FindIncomingOfferFriendshipsResponse(
                    jdbcTemplate.query(
                            SELECT_OFFER_INCOMING,
                            (resultSet, rowNum) -> new OfferInfo(
                                    resultSet.getObject("from_user_id", java.util.UUID.class),
                                    resultSet.getTimestamp("created_at").toInstant()
                            ),
                            request.userId()
                    )
            );
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new DatabaseReadException("Failed to fetch incoming friendship offers for user: " + request.userId(), ex);
        }
    }

    @Override
    public FindOutgoingOfferFriendshipsResponse findOutgoingOfferFriendships(FindOfferFriendshipsRequest request)
            throws DomainException {
        try {
            return new FindOutgoingOfferFriendshipsResponse(
                    jdbcTemplate.query(
                            SELECT_OFFER_OUTGOING,
                            (resultSet, rowNum) -> new OfferInfo(
                                    resultSet.getObject("to_user_id", java.util.UUID.class),
                                    resultSet.getTimestamp("created_at").toInstant()
                            ),
                            request.userId()
                    )
            );
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new DatabaseReadException("Failed to fetch outgoing friendship offers for user " + request.userId(), ex);
        }
    }

    private DomainException handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            CreateOfferFriendshipRequest request
    ) {
        if (ex.getCause() instanceof java.sql.SQLException sqlException) {
            String message = sqlException.getMessage();

            if (message != null) {
                if (message.contains("uq_offer_friendships_users")) {
                    return new DuplicateFriendshipRequestException(
                            "Friendship offer already exists between " + request.fromUserId() + " and " + request.toUserId()
                    );
                }
                if (message.contains("chk_offer_friendships_different_users")) {
                    return new SelfFriendshipException(
                            "User " + request.fromUserId() + " cannot send a friendship offer to themselves"
                    );
                }
            }
        }
        return new DatabaseWriteException("Unexpected data integrity violation during offer friendship creation", ex);
    }
}
