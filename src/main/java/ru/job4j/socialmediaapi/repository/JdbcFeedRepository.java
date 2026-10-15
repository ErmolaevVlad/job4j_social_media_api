package ru.job4j.socialmediaapi.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.job4j.socialmediaapi.exception.DatabaseReadException;
import ru.job4j.socialmediaapi.exception.DomainException;

import java.util.List;
import java.util.UUID;

@Repository
public class JdbcFeedRepository implements FeedRepository {
    private static final String SELECT_FEED = """
                SELECT p.id,
                   p.author_id,
                   p.title,
                   p.content,
                   p.status,
                   p.created_at,
                   p.updated_at
            FROM subscriptions s
            JOIN posts p ON p.author_id = s.followed_id
            WHERE s.follower_id = ?
              AND p.status = 'PUBLISHED'
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT ? OFFSET ?
            """;
    private final JdbcTemplate jdbcTemplate;

    public JdbcFeedRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public FindFeedResponse findUserFeed(FindFeedRequest request) throws DomainException {
        try {
            List<FeedPostInfo> posts = jdbcTemplate.query(
                    SELECT_FEED,
                    (rs, rowNum) -> new FeedPostInfo(
                            rs.getObject("id", UUID.class),
                            rs.getObject("author_id", UUID.class),
                            rs.getString("title"),
                            rs.getString("content"),
                            PostStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at").toInstant(),
                            rs.getTimestamp("updated_at").toInstant()
                    ),
                    request.userId(),
                    request.limit(),
                    request.offset()
            );
            return new FindFeedResponse(posts);
        } catch (DataAccessException ex) {
            throw new DatabaseReadException("Failed to generate news feed for user: " + request.userId(), ex);
        }
    }
}
