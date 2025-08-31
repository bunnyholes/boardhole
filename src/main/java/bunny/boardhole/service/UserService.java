package bunny.boardhole.service;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.user.UserCreateRequest;
import bunny.boardhole.dto.user.UserUpdateRequest;
import bunny.boardhole.dto.user.UserDto;
import bunny.boardhole.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto create(UserCreateRequest req) {
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        User saved = userRepository.save(user);
        return UserDto.from(saved);
    }

    @Transactional(readOnly = true)
    public UserDto get(Long id) {
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
    public Page<UserDto> listWithPaging(Pageable pageable, String search) {
        Page<User> page;
        if (search == null || search.isBlank()) {
            page = userRepository.findAll(pageable);
        } else {
            page = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, search, pageable);
        }
        return page.map(UserDto::from);
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest req) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) return null;
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getEmail() != null) existing.setEmail(req.getEmail());
        if (req.getPassword() != null) existing.setPassword(passwordEncoder.encode(req.getPassword()));
        existing.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(existing);
        return UserDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Deprecated
    public User login(String username, String password) {
        throw new UnsupportedOperationException(
            "Authentication should be handled by Spring Security AuthenticationManager"
        );
    }
    
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
