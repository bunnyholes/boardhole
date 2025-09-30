package dev.xiyo.bunnyholes.boardhole.user.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserProfileImageResult;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.PasswordUpdateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserProfileImageRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserResponse;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserUpdateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserProfileImageCommandMapper;
import dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping(ApiPaths.USERS)
@Validated
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 관리 및 조회 기능")
public class UserController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserWebMapper userWebMapper;
    private final UserProfileImageCommandMapper userProfileImageCommandMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "사용자 목록 조회", description = "[ROLE:ADMIN] 관리자가 사용자 목록을 페이지네이션으로 조회합니다. 검색 기능을 제공합니다.", security = @SecurityRequirement(name = "admin-role"))
    @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공", content = @Content(schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    public Page<UserResponse> list(@Parameter(description = "검색어 (사용자명, 이름, 이메일)") @RequestParam(required = false) @Nullable String search, @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<UserResult> results = search != null && !search.trim().isEmpty() ? userQueryService.listWithPaging(pageable,
                search.trim()) : userQueryService.listWithPaging(pageable);

        return results.map(userWebMapper::toResponse);
    }

    @GetMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 상세 조회", description = "[AUTH] 특정 사용자의 상세 정보를 조회합니다. 관리자이거나 본인만 조회 가능합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 조회 성공", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    public UserResponse get(@Parameter(description = "조회할 사용자명") @PathVariable String username) {
        var userResult = userQueryService.get(username);
        return userWebMapper.toResponse(userResult);
    }

    @PutMapping(value = "/{username}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 정보 수정", description = "[AUTH] 사용자의 개인 정보를 수정합니다. 인증된 사용자만 사용할 수 있습니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = UserUpdateRequest.class))))
    @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "422", description = "유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    public UserResponse update(@Parameter(description = "수정할 사용자명") @PathVariable String username, @Validated @ModelAttribute UserUpdateRequest req) {
        var cmd = userWebMapper.toUpdateCommand(username, req);
        var updated = userCommandService.update(cmd);
        return userWebMapper.toResponse(updated);
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "사용자 삭제", description = "[AUTH] 사용자 계정을 삭제합니다. 인증된 사용자만 사용할 수 있습니다.")
    @ApiResponse(responseCode = "204", description = "사용자 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    public void delete(@Parameter(description = "삭제할 사용자명") @PathVariable String username) {
        userCommandService.delete(username);
    }

    @PatchMapping(value = "/{username}/password", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "패스워드 변경", description = "[AUTH] 사용자의 패스워드를 변경합니다. 현재 패스워드 확인이 필요합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = PasswordUpdateRequest.class))))
    @ApiResponse(responseCode = "204", description = "패스워드 변경 성공")
    @ApiResponse(responseCode = "422", description = "유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "현재 패스워드 불일치")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    public void updatePassword(@Parameter(description = "사용자명") @PathVariable String username, @Validated @ModelAttribute PasswordUpdateRequest req) {
        var cmd = userWebMapper.toUpdatePasswordCommand(username, req);
        userCommandService.updatePassword(cmd);
    }

    @PutMapping(value = "/{username}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "프로필 이미지 업로드/삭제", description = "[AUTH] 프로필 이미지를 업로드하거나 삭제합니다. remove=true일 경우 기존 이미지를 삭제합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = UserProfileImageRequest.class))))
    @ApiResponse(responseCode = "200", description = "프로필 이미지 업로드 성공", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "204", description = "프로필 이미지 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "422", description = "유효하지 않은 파일")
    public ResponseEntity<?> updateProfileImage(@PathVariable String username, @ModelAttribute UserProfileImageRequest request) {
        var cmd = userProfileImageCommandMapper.toCommand(username, request);
        boolean remove = cmd.remove();
        UserResult result = userCommandService.updateProfileImage(cmd);
        if (remove)
            return ResponseEntity.noContent().build();
        return ResponseEntity.ok(userWebMapper.toResponse(result));
    }

    @GetMapping(value = "/{username}/profile-image")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "프로필 이미지 다운로드", description = "[AUTH] 사용자의 프로필 이미지를 바이너리로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "프로필 이미지 다운로드 성공")
    @ApiResponse(responseCode = "404", description = "이미지 없음")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String username) {
        UserProfileImageResult result = userQueryService.getProfileImage(username);
        MediaType mediaType = result.contentType() != null ? MediaType.parseMediaType(result.contentType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(result.size())
                .body(result.data());
    }

    // 이메일 변경 기능은 JWT 기반 인증으로 전환 예정

    @GetMapping(ApiPaths.USERS_ME)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "[AUTH] 현재 로그인한 사용자의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "현재 사용자 정보 조회 성공", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    public UserResponse me(@AuthenticationPrincipal UserDetails principal) {
        UserResult result = userQueryService.get(principal.getUsername());
        return userWebMapper.toResponse(result);
    }
}
