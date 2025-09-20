package dev.xiyo.bunnyholes.boardhole.board.application.command;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import dev.xiyo.bunnyholes.boardhole.board.application.mapper.BoardMapper;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.domain.Board;
import dev.xiyo.bunnyholes.boardhole.board.domain.validation.BoardValidationConstants;
import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.test.ValidationEnabledTestConfig;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import({BoardCommandService.class, ValidationEnabledTestConfig.class})
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BoardCommandService 단위 테스트")
@Tag("unit")
class BoardCommandServiceTest {

    @MockitoBean
    private BoardRepository boardRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private BoardMapper boardMapper;

    @Autowired
    private BoardCommandService boardCommandService;

    private User mockUser;
    private Board mockBoard;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                       .username("testuser")
                       .password("password")
                       .name("TestUser")
                       .email("test@example.com")
                       .roles(Set.of(Role.USER))
                       .build();
        // JPA에서 @GeneratedValue는 저장시에 생성되므로 테스트에서는 수동으로 ID 설정
        ReflectionTestUtils.setField(mockUser, "id", UUID.randomUUID());

        mockBoard = Board.builder()
                         .title("Test Title")
                         .content("Test Content")
                         .author(mockUser)
                         .build();
        // JPA에서 @GeneratedValue는 저장시에 생성되므로 테스트에서는 수동으로 ID 설정
        ReflectionTestUtils.setField(mockBoard, "id", UUID.randomUUID());
    }

    @Nested
    @DisplayName("게시글 생성")
    class CreateBoard {

        @Test
        @DisplayName("✅ 게시글 생성 성공")
        void create_ValidCommand_CreatesBoardSuccessfully() {
            // Given
            CreateBoardCommand cmd = new CreateBoardCommand(
                    mockUser.getId(),
                    "Valid Title",
                    "Valid Content"
            );
            BoardResult expectedResult = new BoardResult(
                    mockBoard.getId(),
                    "Valid Title",
                    "Valid Content",
                    mockUser.getId(),
                    "testuser",
                    0,
                    mockBoard.getCreatedAt(),
                    mockBoard.getUpdatedAt()
            );

            when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(
                    mockUser));
            when(boardRepository.save(any(Board.class))).thenReturn(mockBoard);
            when(boardMapper.toResult(mockBoard)).thenReturn(expectedResult);

            // When
            BoardResult result = boardCommandService.create(cmd);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Valid Title");
            assertThat(result.content()).isEqualTo("Valid Content");
            verify(userRepository).findById(mockUser.getId());
            verify(boardRepository).save(any(Board.class));
            verify(boardMapper).toResult(mockBoard);
        }

        @Test
        @DisplayName("❌ 작성자 미존재 → ResourceNotFoundException with 국제화 메시지")
        void create_UserNotFound_ThrowsResourceNotFoundException() {
            // Given
            CreateBoardCommand cmd = new CreateBoardCommand(
                    UUID.randomUUID(),
                    "Valid Title",
                    "Valid Content"
            );

            when(userRepository.findById(cmd.authorId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> boardCommandService.create(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository).findById(cmd.authorId());
            verifyNoInteractions(boardRepository, boardMapper);
        }

        // 검증 테스트 케이스들

        @Test
        void create_EmptyTitle_ThrowsConstraintViolationException() {
            // Given
            CreateBoardCommand cmd = new CreateBoardCommand(
                    mockUser.getId(),
                    "",
                    "Valid Content"
            );

            // When & Then
            assertThatThrownBy(() -> boardCommandService.create(cmd))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("title");
        }

        @Test
        void create_BlankTitle_ThrowsConstraintViolationException() {
            // Given
            CreateBoardCommand cmd = new CreateBoardCommand(
                    mockUser.getId(),
                    "   ",
                    "Valid Content"
            );

            // When & Then
            assertThatThrownBy(() -> boardCommandService.create(cmd))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("title");
        }

        @Test
        void create_TooLongTitle_ThrowsConstraintViolationException() {
            // Given
            String longTitle = "a".repeat(BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + 1); // 상수 + 1
            CreateBoardCommand cmd = new CreateBoardCommand(
                    mockUser.getId(),
                    longTitle,
                    "Valid Content"
            );

            // Mock 설정 (validation 전에 비즈니스 로직이 실행될 수 있으므로)
            when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(
                    mockUser));

            // When & Then
            assertThatThrownBy(() -> boardCommandService.create(cmd))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("title");
        }

        @Test
        void create_EmptyContent_ThrowsConstraintViolationException() {
            // Given
            CreateBoardCommand cmd = new CreateBoardCommand(
                    mockUser.getId(),
                    "Valid Title",
                    ""
            );

            // When & Then
            assertThatThrownBy(() -> boardCommandService.create(cmd))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("content");
        }

        @Test
        void create_BlankContent_ThrowsConstraintViolationException() {
            // Given
            CreateBoardCommand cmd = new CreateBoardCommand(
                    mockUser.getId(),
                    "Valid Title",
                    "   "
            );

            // When & Then
            assertThatThrownBy(() -> boardCommandService.create(cmd))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("content");
        }

        @Test
        void create_TooLongContent_ThrowsConstraintViolationException() {
            // Given
            String longContent = "a".repeat(BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH + 1); // 상수 + 1
            CreateBoardCommand cmd = new CreateBoardCommand(
                    mockUser.getId(),
                    "Valid Title",
                    longContent
            );

            // Mock 설정 (validation 전에 비즈니스 로직이 실행될 수 있으므로)
            when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(
                    mockUser));

            // When & Then
            assertThatThrownBy(() -> boardCommandService.create(cmd))
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("content");
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdateBoard {

        @Test
        @DisplayName("✅ 게시글 수정 성공")
        void update_ValidCommand_UpdatesBoardSuccessfully() {
            // Given
            UpdateBoardCommand cmd = new UpdateBoardCommand(
                    mockBoard.getId(),
                    "Updated Title",
                    "Updated Content"
            );
            BoardResult expectedResult = new BoardResult(
                    mockBoard.getId(),
                    "Updated Title",
                    "Updated Content",
                    mockUser.getId(),
                    "testuser",
                    0,
                    mockBoard.getCreatedAt(),
                    mockBoard.getUpdatedAt()
            );

            when(boardRepository.findById(mockBoard.getId())).thenReturn(Optional.of(
                    mockBoard));
            when(boardRepository.save(mockBoard)).thenReturn(
                    mockBoard);
            when(boardMapper.toResult(mockBoard)).thenReturn(expectedResult);

            // When
            BoardResult result = boardCommandService.update(cmd);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Updated Title");
            assertThat(result.content()).isEqualTo("Updated Content");
            verify(boardRepository).findById(mockBoard.getId());
            verify(boardRepository).save(mockBoard);
            verify(boardMapper).toResult(mockBoard);
        }

        @Test
        @DisplayName("❌ 게시글 미존재 → ResourceNotFoundException with 국제화 메시지")
        void update_BoardNotFound_ThrowsResourceNotFoundException() {
            // Given
            UpdateBoardCommand cmd = new UpdateBoardCommand(
                    UUID.randomUUID(),
                    "Updated Title",
                    "Updated Content"
            );

            when(boardRepository.findById(cmd.boardId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> boardCommandService.update(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(boardRepository).findById(cmd.boardId());
            verifyNoMoreInteractions(boardRepository);
            verifyNoInteractions(boardMapper);
        }

        @Test
        void update_PartialUpdate_OnlyUpdatesProvidedFields() {
            // Given - title만 업데이트
            UpdateBoardCommand cmd = new UpdateBoardCommand(
                    mockBoard.getId(),
                    "Updated Title Only",
                    null
            );
            BoardResult expectedResult = new BoardResult(
                    mockBoard.getId(),
                    "Updated Title Only",
                    "Test Content", // 기존 content 유지
                    mockUser.getId(),
                    "testuser",
                    0,
                    mockBoard.getCreatedAt(),
                    mockBoard.getUpdatedAt()
            );

            when(boardRepository.findById(mockBoard.getId())).thenReturn(Optional.of(
                    mockBoard));
            when(boardRepository.save(mockBoard)).thenReturn(
                    mockBoard);
            when(boardMapper.toResult(mockBoard)).thenReturn(expectedResult);

            // When
            BoardResult result = boardCommandService.update(cmd);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("Updated Title Only");
            verify(boardRepository).findById(mockBoard.getId());
            verify(boardRepository).save(mockBoard);
            verify(boardMapper).toResult(mockBoard);
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeleteBoard {

        @Test
        @DisplayName("✅ 게시글 삭제 성공")
        void delete_ExistingBoard_DeletesSuccessfully() {
            // Given
            UUID boardId = mockBoard.getId();
            when(boardRepository.findById(boardId)).thenReturn(Optional.of(mockBoard));

            // When
            boardCommandService.delete(boardId);

            // Then
            verify(boardRepository).findById(boardId);
            verify(boardRepository).delete(mockBoard);
        }

        @Test
        @DisplayName("❌ 게시글 미존재 → ResourceNotFoundException with 국제화 메시지")
        void delete_BoardNotFound_ThrowsResourceNotFoundException() {
            // Given
            UUID boardId = UUID.randomUUID();
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> boardCommandService.delete(boardId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(boardRepository).findById(boardId);
            verify(boardRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("조회수 증가")
    class IncrementViewCount {

        @Test
        @DisplayName("✅ 조회수 증가 - save 사용 (saveAndFlush 아님)")
        void incrementViewCount_ExistingBoard_IncrementsSuccessfully() {
            // Given
            IncrementViewCountCommand cmd = new IncrementViewCountCommand(mockBoard.getId());
            when(boardRepository.findById(mockBoard.getId())).thenReturn(Optional.of(
                    mockBoard));
            when(boardRepository.save(mockBoard)).thenReturn(
                    mockBoard);

            // When
            boardCommandService.incrementViewCount(cmd);

            // Then
            verify(boardRepository).findById(mockBoard.getId());
            verify(boardRepository).save(mockBoard);
        }

        @Test
        @DisplayName("❌ 게시글 미존재 → ResourceNotFoundException with 국제화 메시지")
        void incrementViewCount_BoardNotFound_ThrowsResourceNotFoundException() {
            // Given
            IncrementViewCountCommand cmd = new IncrementViewCountCommand(UUID.randomUUID());
            when(boardRepository.findById(cmd.boardId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> boardCommandService.incrementViewCount(cmd))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(boardRepository).findById(cmd.boardId());
            verifyNoMoreInteractions(boardRepository);
        }
    }
}
