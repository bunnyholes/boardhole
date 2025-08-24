package bunny.boardhole.controller;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.user.UserResponse;
import bunny.boardhole.dto.user.UserUpdateRequest;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
        userService.delete(id);
    }
}

