package com.convergentmemory.service;

import com.convergentmemory.dto.AuthRequest;
import com.convergentmemory.dto.AuthResponse;
import com.convergentmemory.entity.User;
import com.convergentmemory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    @Transactional
    public AuthResponse register(AuthRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().length() < 4) {
            throw new IllegalArgumentException("密码至少 4 位");
        }
        if (userRepo.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User u = User.builder()
                .username(req.getUsername().trim())
                .passwordHash(encoder.encode(req.getPassword()))
                .apiToken(UUID.randomUUID().toString().replace("-", ""))
                .displayName(req.getDisplayName() == null ? req.getUsername() : req.getDisplayName())
                .createdAt(LocalDateTime.now())
                .build();
        u = userRepo.save(u);
        log.info("registered new user: {} (id={})", u.getUsername(), u.getId());
        return toResponse(u, "注册成功");
    }

    @Transactional
    public AuthResponse login(AuthRequest req) {
        User u = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        log.info("login success: {} (id={})", u.getUsername(), u.getId());
        return toResponse(u, "登录成功");
    }

    public User findByToken(String token) {
        if (token == null || token.isBlank()) return null;
        return userRepo.findByApiToken(token).orElse(null);
    }

    public User findById(Long id) {
        if (id == null) return null;
        return userRepo.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        if (username == null) return null;
        return userRepo.findByUsername(username).orElse(null);
    }

    private AuthResponse toResponse(User u, String msg) {
        return new AuthResponse(u.getId(), u.getUsername(), u.getDisplayName(),
                u.getApiToken(), msg, "/u/" + u.getUsername());
    }
}
