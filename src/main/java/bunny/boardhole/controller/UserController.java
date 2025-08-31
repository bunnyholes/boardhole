package bunny.boardhole.controller;

import bunny.boardhole.dto.auth.CurrentUserResponse;
import bunny.boardhole.dto.user.UserDto;
import bunny.boardhole.dto.user.UserResponse;
import bunny.boardhole.dto.user.UserUpdateRequest;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.security.AppUserPrincipal;
import bunny.boardhole.service.UserService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PermitAll
    public Page<UserResponse> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search
    ) {
        Page<UserDto> page = search == null
                ? userService.listWithPaging(pageable)
                : userService.listWithPaging(pageable, search);
        return page.map(UserResponse::from);
    }

    @GetMapping("/{id}")
    @PermitAll
    public UserResponse get(@PathVariable Long id) {
        UserDto userDto = userService.get(id);
        if (userDto == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return UserResponse.from(userDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public UserResponse update(@PathVariable Long id, @Validated @ModelAttribute UserUpdateRequest req) {
        UserDto updated = userService.update(id, req);
        if (updated == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return UserResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        UserDto userDto = userService.get(id);
        if (userDto == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userService.delete(id);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public CurrentUserResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            throw new bunny.boardhole.exception.UnauthorizedException("not logged in");
        }
        return CurrentUserResponse.from(principal.getUser());
    }
}
