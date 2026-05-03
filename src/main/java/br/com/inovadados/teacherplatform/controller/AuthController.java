package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.dto.request.LoginRequest;
import br.com.inovadados.teacherplatform.dto.response.LoginResponse;
import br.com.inovadados.teacherplatform.service.AuthLoginResult;
import br.com.inovadados.teacherplatform.service.AuthService;
import br.com.inovadados.teacherplatform.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        rateLimitService.checkRateLimit(getClientIp(httpRequest));

        AuthLoginResult result = authService.login(request, httpRequest);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.refreshToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(Duration.ofDays(30))
            .path("/auth")
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(result.loginResponse());
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
