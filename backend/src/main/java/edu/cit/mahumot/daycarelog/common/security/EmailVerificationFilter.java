package edu.cit.mahumot.daycarelog.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class EmailVerificationFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/me",
            "/api/auth/refresh-token",
            "/api/auth/logout"
    );

    private final JwtUtil jwtUtil;

    public EmailVerificationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ") && !ALLOWED_PATHS.contains(request.getRequestURI())) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token) && !jwtUtil.extractEmailVerified(token)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\":\"Please verify your email address to continue.\",\"code\":\"EMAIL_NOT_VERIFIED\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
