package bunny.boardhole.controller;

import bunny.boardhole.dto.auth.CurrentUserResponse;
import bunny.boardhole.dto.user.UserResponse;
import bunny.boardhole.dto.user.UserDto;
import bunny.boardhole.dto.user.UserUpdateRequest;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.service.UserService;
import bunny.boardhole.security.AppUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Page<UserResponse> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search
    ) {
        Page<UserDto> page = userService.listWithPaging(pageable, search);
        return page.map(dto -> UserResponse.from(dto.toEntity()));
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        UserDto userDto = userService.get(id);
        if (userDto == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return UserResponse.from(userDto.toEntity());
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        UserDto updated = userService.update(id, req);
        if (updated == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return UserResponse.from(updated.toEntity());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        UserDto userDto = userService.get(id);
        if (userDto == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userService.delete(id);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            throw new bunny.boardhole.exception.UnauthorizedException("not logged in");
        }
        return CurrentUserResponse.from(principal.getUser());
    }
}
