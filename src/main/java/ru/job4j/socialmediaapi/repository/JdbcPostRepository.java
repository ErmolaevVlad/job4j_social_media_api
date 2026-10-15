package ru.job4j.socialmediaapi.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.job4j.socialmediaapi.exception.DatabaseReadException;
import ru.job4j.socialmediaapi.exception.DatabaseWriteException;
import ru.job4j.socialmediaapi.exception.DomainException;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPostRepository implements PostRepository {

    private static final String CREATE_POST = """
            INSERT INTO posts (
                id, author_id, title,
                content, status, created_at,
                updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id, author_id, title,
                      content, status,
                      created_at, updated_at
            """;

    private static final String FIND_BY_ID = """
            SELECT id, author_id, title, content, status, created_at, updated_at
            FROM posts
            WHERE id = ?
            """;

    private static final String FIND_ALL_PUBLISHED = """
            SELECT id, author_id, title, content, status, created_at, updated_at
            FROM posts
            WHERE status = 'PUBLISHED'
            """;

    private static final String UPDATE_POST = """
            UPDATE posts
            SET title = ?, content = ?, updated_at = ?
            WHERE id = ? and author_id = ?
            """;

    private static final String SOFT_DELETE_POST = """
            UPDATE posts
            SET status = 'DELETED', updated_at = ?
            WHERE id = ? AND status != 'DELETED'
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcPostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CreatePostResponse createPost(CreatePostRequest request) throws DomainException {
        try {
            return jdbcTemplate.queryForObject(
                    CREATE_POST,
                    (resultSet, rowNum) -> new CreatePostResponse(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getObject("author_id", UUID.class),
                            resultSet.getString("title"),
                            resultSet.getString("content"),
                            PostStatus.valueOf(resultSet.getString("status")),
                            resultSet.getTimestamp("created_at").toInstant(),
                            resultSet.getTimestamp("updated_at").toInstant()
                    ),
                    request.id(),
                    request.authorId(),
                    request.title(),
                    request.content(),
                    request.status().name(),
                    Timestamp.from(request.createdAt()),
                    Timestamp.from(request.updatedAt())
            );
        } catch (DataIntegrityViolationException ex) {
            throw new DatabaseWriteException("Failed to create post due to integrity constraint violation", ex);
        }
    }

    @Override
    public Optional<FindPostResponse> findById(FindPostRequest request) throws DomainException {
        try {
            FindPostResponse response = jdbcTemplate.queryForObject(
                    FIND_BY_ID,
                    (rs, rowNum) -> new FindPostResponse(
                            rs.getObject("id", UUID.class),
                            rs.getObject("author_id", UUID.class),
                            rs.getString("title"),
                            rs.getString("content"),
                            PostStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at").toInstant(),
                            rs.getTimestamp("updated_at").toInstant()
                    ),
                    request.id()
            );
            return Optional.ofNullable(response);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        } catch (DataAccessException ex) {
            throw new DatabaseReadException("Failed to fetch post with ID: " + request.id(), ex);
        }
    }

    @Override
    public FindAllActivePostsResponse findAllPublished() throws DomainException {
        try {
            var posts = jdbcTemplate.query(
                    FIND_ALL_PUBLISHED,
                    (rs, rowNum) -> new PostInfo(
                            rs.getObject("id", UUID.class),
                            rs.getObject("author_id", UUID.class),
                            rs.getString("title"),
                            rs.getString("content"),
                            PostStatus.valueOf(rs.getString("status")),
                            rs.getTimestamp("created_at").toInstant(),
                            rs.getTimestamp("updated_at").toInstant()
                    )
            );
            return new FindAllActivePostsResponse(posts);
        } catch (DataAccessException ex) {
            throw new DatabaseReadException("Failed to fetch active posts", ex);
        }
    }

    @Override
    public UpdatePostResponse update(UpdatePostRequest request) throws DomainException {
        try {
            int rowsAffected = jdbcTemplate.update(
                    UPDATE_POST,
                    request.title(),
                    request.content(),
                    Timestamp.from(java.time.Instant.now()),
                    request.id(),
                    request.authorId()

            );
            return new UpdatePostResponse(rowsAffected > 0);
        } catch (DataAccessException ex) {
            throw new DatabaseWriteException("Failed to update post with ID: " + request.id(), ex);
        }
    }

    @Override
    public DeletePostResponse delete(DeletePostRequest request) throws DomainException {
        try {
            int rowsAffected = jdbcTemplate.update(
                    SOFT_DELETE_POST,
                    Timestamp.from(java.time.Instant.now()),
                    request.id()
            );
            return new DeletePostResponse(rowsAffected > 0);
        } catch (DataAccessException ex) {
            throw new DatabaseWriteException("Failed to soft-deleteSubscription post with ID: " + request.id(), ex);
        }
    }
}
