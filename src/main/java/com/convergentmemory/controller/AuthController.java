package com.convergentmemory.controller;

import com.convergentmemory.dto.AuthRequest;
import com.convergentmemory.dto.AuthResponse;
import com.convergentmemory.entity.User;
import com.convergentmemory.repository.UserRepository;
import com.convergentmemory.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepo;

    private static final String BASE_URL = "http://134.209.66.112";

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req, HttpSession session) {
        try {
            AuthResponse resp = authService.register(req);
            session.setAttribute("userId", resp.getUserId());
            return ResponseEntity.ok(enrichWithUrls(resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req, HttpSession session) {
        try {
            AuthResponse resp = authService.login(req);
            session.setAttribute("userId", resp.getUserId());
            return ResponseEntity.ok(enrichWithUrls(resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 云便签式零注册:GET /api/quick/{username}
     * - 用户名不存在 → 自动创建账号,密码=随机,token 返回
     * - 用户名已存在 → 返回错误,提示加密码访问
     * 这是最 AI 友好的接口:豆包说"帮我建一个 xiaolin 的便签"一行就能完成
     */
    @GetMapping("/quick/{username}")
    public ResponseEntity<?> quickCreate(@PathVariable String username) {
        if (username == null || username.length() < 3 || !username.matches("[a-zA-Z0-9_-]+")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "用户名至少 3 位,只能包含字母、数字、下划线、横线"
            ));
        }
        Optional<User> existing = userRepo.findByUsername(username);
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "这个用户名已经被使用",
                    "hint", "如果是你的账号,请用 /api/auth/login 登录;否则换一个用户名"
            ));
        }

        AuthRequest req = new AuthRequest();
        req.setUsername(username);
        req.setPassword(UUID.randomUUID().toString().substring(0, 12));
        req.setDisplayName(username);
        AuthResponse resp = authService.register(req);

        String tokenSuffix = "?token=" + resp.getApiToken();
        return ResponseEntity.ok(Map.of(
                "username", resp.getUsername(),
                "apiToken", resp.getApiToken(),
                "myMemoryUrl", BASE_URL + "/u/" + resp.getUsername() + tokenSuffix,
                "memoriesJsonUrl", BASE_URL + "/api/u/" + resp.getUsername() + "/memories.json" + tokenSuffix,
                "agentGuideUrl", BASE_URL + "/u/" + resp.getUsername() + "/agent-guide" + tokenSuffix,
                "addMemoryCurl", "curl -X POST " + BASE_URL + "/api/memory/add -H 'Authorization: Bearer "
                        + resp.getApiToken() + "' -H 'Content-Type: application/json' -d '{\"title\":\"...\",\"content\":\"...\",\"cueTags\":[\"...\"]}'",
                "message", "🎉 便签创建成功!保存好 myMemoryUrl(自己用)和 agentGuideUrl(给 AI 看的使用手册)。把 agentGuideUrl 复制给豆包/ChatGPT/Claude,它读完就懂全部操作。"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "已退出登录"));
    }

    private Map<String, Object> enrichWithUrls(AuthResponse resp) {
        return Map.of(
                "userId", resp.getUserId(),
                "username", resp.getUsername(),
                "displayName", resp.getDisplayName(),
                "apiToken", resp.getApiToken(),
                "message", resp.getMessage(),
                "myMemoryUrl", BASE_URL + "/u/" + resp.getUsername() + "?token=" + resp.getApiToken(),
                "memoriesJsonUrl", BASE_URL + "/api/u/" + resp.getUsername() + "/memories.json?token=" + resp.getApiToken(),
                "homepageUrl", resp.getHomepageUrl()
        );
    }
}
