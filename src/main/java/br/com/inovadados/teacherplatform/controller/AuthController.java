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
import org.springframework.web.bind.annotation.CookieValue;
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

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.refreshToken()).toString())
            .body(result.loginResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        AuthLoginResult result = authService.refresh(refreshToken);

        ResponseCookie novoCookie = buildRefreshCookie(result.refreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, novoCookie.toString())
            .body(result.loginResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        authService.logout(refreshToken);

        ResponseCookie clearCookie = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(0)
            .path("/auth")
            .build();

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(Duration.ofDays(30))
            .path("/auth")
            .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
