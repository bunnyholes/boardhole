package bunny.boardhole.service;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.user.UserCreateRequest;
import bunny.boardhole.dto.user.UserDto;
import bunny.boardhole.dto.user.UserUpdateRequest;
import bunny.boardhole.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto create(@Valid UserCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email already exists");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .email(req.getEmail())
                .build();
        User saved = userRepository.save(user);
        return UserDto.from(saved);
    }

    @Transactional(readOnly = true)
    public UserDto get(@NotNull @Positive Long id) {
        User user = userRepository.findById(id).orElse(null);
        return user != null ? UserDto.from(user) : null;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(UserDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserDto> listWithPaging(@NotNull Pageable pageable) {
        return userRepository.findAll(pageable).map(UserDto::from);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> listWithPaging(@NotNull Pageable pageable, @NotBlank String search) {
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                search, search, search, pageable).map(UserDto::from);
    }

    @Transactional
    public UserDto update(@NotNull @Positive Long id, @Valid UserUpdateRequest req) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) return null;
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getEmail() != null) existing.setEmail(req.getEmail());
        if (req.getPassword() != null) existing.setPassword(passwordEncoder.encode(req.getPassword()));
        User saved = userRepository.save(existing);
        return UserDto.from(saved);
    }

    @Transactional
    public void delete(@NotNull @Positive Long id) {
        userRepository.deleteById(id);
    }

    @Deprecated
    public User login(String username, String password) {
        throw new UnsupportedOperationException(
            "Authentication should be handled by Spring Security AuthenticationManager"
        );
    }
    
    @Transactional
    public void updateLastLogin(@NotNull @Positive Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
