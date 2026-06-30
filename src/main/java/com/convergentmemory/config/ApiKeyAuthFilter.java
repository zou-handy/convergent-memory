package com.convergentmemory.config;

import com.convergentmemory.entity.User;
import com.convergentmemory.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${memory.api-key}")
    private String legacyKey;

    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring("Bearer ".length()).trim();
        }
        if (token == null) {
            token = request.getParameter("token");
        }

        if (token != null && !token.isBlank()) {
            // 1) 真用户 token 匹配
            User user = authService.findByToken(token);
            if (user != null) {
                var authn = new UsernamePasswordAuthenticationToken(
                        user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authn);
            } else if (token.equals(legacyKey)) {
                // 2) MVP 兼容老 API key,作为 user_id=1(系统主人)
                User owner = authService.findById(1L);
                if (owner != null) {
                    var authn = new UsernamePasswordAuthenticationToken(
                            owner, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authn);
                }
            }
        }

        // session 路径:HttpSession 里有 userId 也算登录
        Object sessionUid = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("userId");
        if (sessionUid instanceof Long uid && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = authService.findById(uid);
            if (user != null) {
                var authn = new UsernamePasswordAuthenticationToken(
                        user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authn);
            }
        }

        chain.doFilter(request, response);
    }
}
