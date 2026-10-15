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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Testcontainers
@Transactional
@SpringBootTest
class JdbcFeedRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private FeedRepository feedRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private PostRepository postRepository;

    private static final UUID FOLLOWER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FOLLOWED = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID STRANGER = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant FIXED_NOW = Instant.parse("2026-09-22T10:15:00Z");

    @Test
    @DisplayName("Успех - опубликованный пост появляется в ленте если есть подписка")
    void whenUserHasSubscriptionAndPublishedPostThenPostIsPresentInFeed() throws DomainException {
        var id = UUID.randomUUID();
        var subscriptionRequest = new SubscriptionRepository.CreateFollowerRequest(
                id,
                FOLLOWER,
                FOLLOWED,
                FIXED_NOW
        );
        subscriptionRepository.createFollower(subscriptionRequest);
        var postId = UUID.randomUUID();
        var postRequest = new PostRepository.CreatePostRequest(
                postId,
                FOLLOWED,
                "First Post",
                "test content",
                PostStatus.PUBLISHED,
                FIXED_NOW,
                FIXED_NOW
                );
        postRepository.createPost(postRequest);
        var rsl = feedRepository.findUserFeed(
                new FeedRepository.FindFeedRequest(FOLLOWER, 5, 0)
        );
        var excepted = new FeedRepository.FindFeedResponse(
                List.of(new FeedRepository.FeedPostInfo(
                        postId,
                        FOLLOWED,
                        "First Post",
                        "test content",
                        PostStatus.PUBLISHED,
                        FIXED_NOW,
                        FIXED_NOW
                ))
        );
        assertThat(rsl).isEqualTo(excepted);
    }

    @Test
    @DisplayName("Успех - не возвращает посты пользователей, на которых нет подписки")
    void whenPostFromNotFollowedUserThenItShouldNotBeInFeed() throws DomainException {

        subscriptionRepository.createFollower(new SubscriptionRepository.CreateFollowerRequest(
                UUID.randomUUID(), FOLLOWER, FOLLOWED, FIXED_NOW
        ));

        var postId = UUID.randomUUID();
        postRepository.createPost(new PostRepository.CreatePostRequest(
                postId, FOLLOWED, "Valid Post", "content",
                PostStatus.PUBLISHED, FIXED_NOW, FIXED_NOW
        ));

        var strangerPostId = UUID.randomUUID();
        postRepository.createPost(new PostRepository.CreatePostRequest(
                strangerPostId, STRANGER, "Stranger Post", "content",
                PostStatus.PUBLISHED, FIXED_NOW, FIXED_NOW
        ));

        var rsl = feedRepository.findUserFeed(
                new FeedRepository.FindFeedRequest(FOLLOWER, 10, 0)
        );

        assertThat(rsl.posts())
                .hasSize(1)
                .extracting("postId")
                .containsExactly(postId)
                .doesNotContain(strangerPostId);
    }

    @Test
    @DisplayName("Успех - после удаления подписки лента становится пустой")
    void whenSubscriptionDeletedThenFeedShouldBeEmpty() throws DomainException {
        var subscriptionId = UUID.randomUUID();
        var subscriptionRequest = new SubscriptionRepository.CreateFollowerRequest(
                subscriptionId, FOLLOWER, FOLLOWED, FIXED_NOW
        );
        subscriptionRepository.createFollower(subscriptionRequest);

        var postId = UUID.randomUUID();
        postRepository.createPost(new PostRepository.CreatePostRequest(
                postId, FOLLOWED, "Post Title", "content",
                PostStatus.PUBLISHED, FIXED_NOW, FIXED_NOW
        ));

        var initialFeed = feedRepository.findUserFeed(
                new FeedRepository.FindFeedRequest(FOLLOWER, 10, 0)
        );
        assertThat(initialFeed.posts()).hasSize(1);

        subscriptionRepository.deleteSubscription(
                new SubscriptionRepository.DeleteSubscriptionRequest(FOLLOWER, FOLLOWED)
        );
        var rsl = feedRepository.findUserFeed(
                new FeedRepository.FindFeedRequest(FOLLOWER, 10, 0)
        );
        assertThat(rsl.posts()).isEmpty();
    }

    @Test
    @DisplayName("Успех - исключает посты со статусом DELETED")
    void whenPostHasDeleteStatusThenItShouldNotBeInFeed() throws DomainException {

        subscriptionRepository.createFollower(new SubscriptionRepository.CreateFollowerRequest(
                UUID.randomUUID(), FOLLOWER, FOLLOWED, FIXED_NOW
        ));

        var postId = UUID.randomUUID();
        postRepository.createPost(new PostRepository.CreatePostRequest(
                postId, FOLLOWED, "Valid Post", "content",
                PostStatus.PUBLISHED, FIXED_NOW, FIXED_NOW
        ));

        var deleteStatusPostId = UUID.randomUUID();
        postRepository.createPost(new PostRepository.CreatePostRequest(
                deleteStatusPostId, FOLLOWED, "Post deleted status", "content",
                PostStatus.DELETED, FIXED_NOW, FIXED_NOW
        ));

        var rsl = feedRepository.findUserFeed(
                new FeedRepository.FindFeedRequest(FOLLOWER, 10, 0)
        );

        assertThat(rsl.posts())
                .hasSize(1)
                .extracting("postId")
                .containsExactly(postId)
                .doesNotContain(deleteStatusPostId);
    }
}