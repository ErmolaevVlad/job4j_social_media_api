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
import ru.job4j.socialmediaapi.exception.DomainException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@Transactional
@SpringBootTest
class JdbcPostRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private PostRepository postRepository;

    private static final Instant FIXED_NOW = Instant.parse("2026-09-22T10:15:00Z");

    @Test
    @DisplayName("Успех - Успешное сохранение поста и возврат данных")
    void whenCreatePostThenPostIsSavedCorrectly() throws DomainException {
        var postId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var createRequest = new PostRepository.CreatePostRequest(
                postId,
                authorId,
                "Title", "Content",
                PostStatus.PUBLISHED,
                FIXED_NOW, FIXED_NOW
        );

        var rsl = postRepository.createPost(createRequest);
        var excepted = new PostRepository.CreatePostResponse(
                postId,
                authorId,
                "Title", "Content",
                PostStatus.PUBLISHED,
                FIXED_NOW, FIXED_NOW
        );
        assertThat(rsl).isEqualTo(excepted);
    }

    @Test
    @DisplayName("Успех - Поиск по ID. Возвращает Optional с постом, если он существует")
    void whenPostExistsThenFindByIdReturnsOptionalWithPost() throws DomainException {
        var postId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        createPost(postId, authorId);

        var foundOpt = postRepository.findById(new PostRepository.FindPostRequest(postId));

        assertThat(foundOpt).isPresent();
        var post = foundOpt.get();
        assertThat(post.title()).isEqualTo("Title");
        assertThat(post.authorId()).isEqualTo(authorId);
    }

    @Test
    @DisplayName("Успех - Поиск по ID. Возвращает Optional.empty(), если поста нет")
    void whenPostDoesNotExistThenFindByIdReturnsEmptyOptional() throws DomainException {
        var foundOpt = postRepository.findById(new PostRepository.FindPostRequest(UUID.randomUUID()));
        assertThat(foundOpt).isEmpty();
    }

    @Test
    @DisplayName("Успех - Возвращает только посты со статусом PUBLISHED")
    void whenFindAllPublishedThenReturnsOnlyPublishedPosts() throws DomainException {
        var activePostId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        createPost(activePostId, authorId);

        var deletedPostId = UUID.randomUUID();
        postRepository.createPost(new PostRepository.CreatePostRequest(
                deletedPostId,
                authorId,
                "Title deleted",
                "post",
                PostStatus.DELETED,
                FIXED_NOW,
                FIXED_NOW
        ));

        var rsl = postRepository.findAllPublished();

        assertThat(rsl.posts())
                .extracting("id")
                .contains(activePostId)
                .doesNotContain(deletedPostId);
    }

    @Test
    @DisplayName("Успех - изменяет title и content существующего поста")
    void whenUpdatePostThenTitleAndContentAreChanged() throws DomainException {
        var postId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        createPost(postId, authorId);

        var updateRequest = new PostRepository.UpdatePostRequest(
                postId,
                authorId,
                "New Title", "New Content"
        );
        var rsl = postRepository.update(updateRequest);
        assertThat(rsl.isUpdated()).isTrue();

        var updatedPostOpt = postRepository.findById(new PostRepository.FindPostRequest(postId));
        assertThat(updatedPostOpt).isPresent();
        assertThat(updatedPostOpt.get().title()).isEqualTo("New Title");
        assertThat(updatedPostOpt.get().content()).isEqualTo("New Content");
    }

    @Test
    @DisplayName("Успех - Изменяет статус поста на DELETED")
    void whenDeletePostThenStatusChangesToDeleted() throws DomainException {
        var postId = UUID.randomUUID();
        var authorID = UUID.randomUUID();
        createPost(postId, authorID);

        var deleteResponse = postRepository.delete(new PostRepository.DeletePostRequest(postId));
        var rsl = postRepository.findById(new PostRepository.FindPostRequest(postId));

        assertThat(deleteResponse.isDeleted()).isTrue();
        assertThat(rsl).isPresent();
        assertThat(rsl.get().status()).isEqualTo(PostStatus.DELETED);
    }

    private void createPost(UUID id, UUID authorId) throws DomainException {
        var createPostRequest = new PostRepository.CreatePostRequest(
                id,
                authorId,
                "Title",
                "Content",
                PostStatus.PUBLISHED,
                FIXED_NOW,
                FIXED_NOW
        );
        postRepository.createPost(createPostRequest);
    }
}