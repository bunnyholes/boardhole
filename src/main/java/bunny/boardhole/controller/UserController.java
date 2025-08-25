package bunny.boardhole.controller;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.auth.CurrentUserResponse;
import bunny.boardhole.dto.user.UserResponse;
import bunny.boardhole.dto.user.UserUpdateRequest;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @GetMapping
    public List<UserResponse> list() {
        return userService.list().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        User user = userService.get(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return UserResponse.from(user);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        User updated = userService.update(id, req);
        if (updated == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return UserResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        User user = userService.get(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userService.delete(id);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new bunny.boardhole.exception.UnauthorizedException("not logged in");
        }
        return CurrentUserResponse.from(user);
    }
}
