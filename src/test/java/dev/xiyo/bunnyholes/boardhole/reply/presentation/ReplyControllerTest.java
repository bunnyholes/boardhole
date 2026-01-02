package dev.xiyo.bunnyholes.boardhole.reply.presentation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.auth.infrastructure.security.CustomAuthenticationSuccessHandler;
import dev.xiyo.bunnyholes.boardhole.reply.application.command.ReplyCommandService;
import dev.xiyo.bunnyholes.boardhole.reply.application.query.ReplyQueryService;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyResult;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyTreeResult;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.mapper.ReplyWebMapperImpl;
import dev.xiyo.bunnyholes.boardhole.shared.config.SecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = ReplyController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
    }
)
@Import({SecurityConfig.class, ReplyWebMapperImpl.class})
class ReplyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReplyCommandService replyCommandService;

    @MockitoBean
    ReplyQueryService replyQueryService;

    @MockitoBean
    PermissionEvaluator permissionEvaluator;

    @MockitoBean
    UserQueryService userQueryService;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    UUID boardId;
    UUID replyId;
    ReplyResult replyResult;

    @BeforeEach
    void setUp() {
        boardId = UUID.randomUUID();
        replyId = UUID.randomUUID();
        replyResult = new ReplyResult(
            replyId,
            boardId,
            null,
            "테스트 댓글",
            UUID.randomUUID(),
            "testuser",
            LocalDateTime.now(),
            null,
            false,
            0,
            List.of()
        );
    }

    @Test
    @DisplayName("댓글 트리 조회 - 성공")
    void getReplyTree_Success() throws Exception {
        var treeResult = new ReplyTreeResult(List.of(replyResult), 1);
        when(replyQueryService.getReplyTree(boardId)).thenReturn(treeResult);

        mockMvc.perform(get("/api/boards/{boardId}/replies", boardId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(1))
            .andExpect(jsonPath("$.replies[0].content").value("테스트 댓글"));
    }

    @Test
    @DisplayName("댓글 트리 조회 - 빈 목록")
    void getReplyTree_Empty() throws Exception {
        var emptyResult = new ReplyTreeResult(List.of(), 0);
        when(replyQueryService.getReplyTree(boardId)).thenReturn(emptyResult);

        mockMvc.perform(get("/api/boards/{boardId}/replies", boardId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(0))
            .andExpect(jsonPath("$.replies").isEmpty());
    }

    @Test
    @DisplayName("댓글 삭제 - 인증 안됨")
    void delete_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/replies/{replyId}", replyId)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }
}
