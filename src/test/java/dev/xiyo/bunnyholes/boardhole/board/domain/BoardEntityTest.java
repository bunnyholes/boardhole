package dev.xiyo.bunnyholes.boardhole.board.domain;

import java.time.LocalDateTime;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import dev.xiyo.bunnyholes.boardhole.board.domain.validation.BoardValidationConstants;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.testsupport.jpa.EntityTestBase;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Board 엔티티 테스트")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("entity")
@Tag("jpa")
class BoardEntityTest extends EntityTestBase {

    @Nested
    @DisplayName("생성자 및 빌더 테스트")
    @Tag("creation")
    class BoardCreation {

        @Test
        @DisplayName("✅ 빌더를 사용한 Board 생성 테스트")
        void createBoard_WithBuilder_Success() {
            // given
            User author = createAndPersistUser();
            final String title = EntityTestBase.TEST_BOARD_TITLE;
            final String content = EntityTestBase.TEST_BOARD_CONTENT;

            // when
            Board board = Board.builder().title(title).content(content).author(author).build();

            // then
            assertThat(board.getTitle()).isEqualTo(title);
            assertThat(board.getContent()).isEqualTo(content);
            assertThat(board.getAuthor()).isEqualTo(author);
            assertThat(board.getViewCount()).isEqualTo(0);
            assertThat(board.getCreatedAt()).isNull();  // BaseEntity의 필드는 persist 전까지 null
            assertThat(board.getUpdatedAt()).isNull();  // BaseEntity의 필드는 persist 전까지 null
        }
    }

    @Nested
    @DisplayName("필수 필드 검증 테스트")
    @Tag("validation")
    class RequiredFieldValidation {

        @Test
        @DisplayName("❌ 빈 제목으로 Board 생성 시 예외 발생")
        void createBoard_WithEmptyTitle_ThrowsException() {
            // given
            User author = createAndPersistUser();
            String expectedMessage = MessageUtils.get("validation.board.title.required");

            // when & then
            assertThatThrownBy(() -> {
                Board board = Board.builder().title("").content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
                persistAndFlush(board);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 빈 내용으로 Board 생성 시 예외 발생")
        void createBoard_WithEmptyContent_ThrowsException() {
            // given
            User author = createAndPersistUser();
            String expectedMessage = MessageUtils.get("validation.board.content.required");

            // when & then
            assertThatThrownBy(() -> {
                Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content("").author(author).build();
                persistAndFlush(board);
            }).isInstanceOf(ConstraintViolationException.class);
        }

    }

    @Nested
    @DisplayName("길이 제한 검증 테스트")
    @Tag("validation")
    class LengthValidation {

        @Test
        @DisplayName("❌ 제목이 최대 길이를 초과할 때 예외 발생")
        void createBoard_WithTitleTooLong_ThrowsException() {
            // given
            User author = createAndPersistUser();
            String longTitle = "a".repeat(BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + 1);
            String expectedMessage = MessageUtils.get("validation.board.title.too-long", BoardValidationConstants.BOARD_TITLE_MAX_LENGTH);

            // when & then
            assertThatThrownBy(() -> {
                Board board = Board.builder().title(longTitle).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
                persistAndFlush(board);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("❌ 내용이 최대 길이를 초과할 때 예외 발생")
        void createBoard_WithContentTooLong_ThrowsException() {
            // given
            User author = createAndPersistUser();
            String longContent = "a".repeat(BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH + 1);
            String expectedMessage = MessageUtils.get("validation.board.content.too-long", BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH);

            // when & then
            assertThatThrownBy(() -> {
                Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(longContent).author(author).build();
                persistAndFlush(board);
            }).isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Nested
    @DisplayName("JPA 생명주기 테스트")
    @Tag("lifecycle")
    class JpaLifecycle {

        @Test
        @DisplayName("✅ @PrePersist 테스트 - 생성 시 시간 필드 자동 설정")
        void prePersist_SetsTimestampsAndDefaultValues() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();

            // when
            persistAndFlush(board);

            // then
            assertThat(board.getCreatedAt()).isNotNull();
            assertThat(board.getUpdatedAt()).isNotNull();
            assertThat(board.getViewCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("✅ @PreUpdate 테스트 - 수정 시 updatedAt 갱신")
        void preUpdate_UpdatesTimestamp() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
            persistAndFlush(board);
            LocalDateTime originalUpdatedAt = board.getUpdatedAt();

            // when
            board.setTitle("새로운 제목");
            entityManager.flush();

            // then
            assertThat(board.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    @Tag("business")
    class BusinessMethods {

        @Test
        @DisplayName("✅ setTitle 테스트 - 정상적인 제목 변경")
        void setTitle_WithValidTitle_Success() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
            final String newTitle = "새로운 제목";

            // when
            board.setTitle(newTitle);

            // then
            assertThat(board.getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("❌ setTitle 테스트 - 빈 제목으로 변경 시 예외 발생")
        void setTitle_WithEmptyTitle_ThrowsException() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
            String expectedMessage = MessageUtils.get("validation.board.title.required");

            // when & then
            assertThatThrownBy(() -> {
                board.setTitle("");
                persistAndFlush(board);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("✅ setContent 테스트 - 정상적인 내용 변경")
        void setContent_WithValidContent_Success() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
            final String newContent = "새로운 내용";

            // when
            board.setContent(newContent);

            // then
            assertThat(board.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("❌ setContent 테스트 - 빈 내용으로 변경 시 예외 발생")
        void setContent_WithEmptyContent_ThrowsException() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
            String expectedMessage = MessageUtils.get("validation.board.content.required");

            // when & then
            assertThatThrownBy(() -> {
                board.setContent("");
                persistAndFlush(board);
            }).isInstanceOf(ConstraintViolationException.class);
        }

        @Test
        @DisplayName("✅ increaseViewCount 테스트 - 조회수 증가")
        void increaseViewCount_IncrementsCorrectly() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
            persistAndFlush(board);

            // when
            board.increaseViewCount();

            // then
            assertThat(board.getViewCount()).isEqualTo(1);

            // when
            board.increaseViewCount();

            // then
            assertThat(board.getViewCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("JPA 영속성 테스트")
    @Tag("persistence")
    class JpaPersistence {

        @Test
        @DisplayName("✅ JPA 저장 및 조회 테스트")
        void saveAndFind_PersistsCorrectly() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();

            // when
            persistAndFlush(board);
            entityManager.clear();
            Board foundBoard = entityManager.find(Board.class, board.getId());

            // then
            assertThat(foundBoard).isNotNull();
            assertThat(foundBoard.getTitle()).isEqualTo(EntityTestBase.TEST_BOARD_TITLE);
            assertThat(foundBoard.getContent()).isEqualTo(EntityTestBase.TEST_BOARD_CONTENT);
            assertThat(foundBoard.getAuthor().getId()).isEqualTo(author.getId());
            assertThat(foundBoard.getViewCount()).isEqualTo(0);
            assertThat(foundBoard.getCreatedAt()).isNotNull();
            assertThat(foundBoard.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("✅ @Version 필드 낙관적 락 테스트")
        void version_OptimisticLocking_WorksCorrectly() {
            // given
            User author = createAndPersistUser();
            Board board = Board.builder().title(EntityTestBase.TEST_BOARD_TITLE).content(EntityTestBase.TEST_BOARD_CONTENT).author(author).build();
            persistAndFlush(board);
            Long originalVersion = board.getVersion();

            // when
            board.setTitle("새로운 제목");
            entityManager.flush();

            // then
            assertThat(board.getVersion()).isGreaterThan(originalVersion);
        }
    }

}
