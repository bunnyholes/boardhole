package bunny.boardhole.service;

import bunny.boardhole.domain.User;
import bunny.boardhole.dto.user.UserCreateRequest;
import bunny.boardhole.dto.user.UserUpdateRequest;
import bunny.boardhole.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public User create(UserCreateRequest req) {
        if (userMapper.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }
        if (userMapper.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("email already exists");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(req.getPassword()) // no hashing per requirements
                .name(req.getName())
                .email(req.getEmail())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return userMapper.findById(user.getId());
    }

    public User get(Long id) {
        return userMapper.findById(id);
    }

    public List<User> list() {
        return userMapper.findAll();
    }

    public User update(Long id, UserUpdateRequest req) {
        User existing = userMapper.findById(id);
        if (existing == null) return null;
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getEmail() != null) existing.setEmail(req.getEmail());
        if (req.getPassword() != null) existing.setPassword(req.getPassword());
        existing.setUpdatedAt(LocalDateTime.now());
        userMapper.update(existing);
        return userMapper.findById(id);
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        userMapper.updateLastLogin(user.getId(), LocalDateTime.now());
        return user;
    }
}

