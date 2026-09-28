package ru.job4j.socialmediaapi.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

    private final JdbcTemplate jdbcTemplate;

    public JdbcOfferFriendshipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreateOfferFriendshipResponse createOfferFriendship(
            CreateOfferFriendshipRequest request
    ) {
        return jdbcTemplate.queryForObject(
                CREATE,
                (resultSet, rowNum) -> new CreateOfferFriendshipResponse(
                        resultSet.getObject("id", java.util.UUID.class),
                        resultSet.getObject("from_user_id", java.util.UUID.class),
                        resultSet.getObject("to_user_id", java.util.UUID.class),
                        resultSet.getString("status"),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant()
                ),
                request.id(),
                request.fromUserId(),
                request.toUserId(),
                request.status(),
                java.sql.Timestamp.from(request.createdAt()),
                java.sql.Timestamp.from(request.updatedAt())
        );
    }
}
