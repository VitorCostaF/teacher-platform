package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Sessao;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.dto.request.LoginRequest;
import br.com.inovadados.teacherplatform.dto.response.LoginResponse;
import br.com.inovadados.teacherplatform.dto.response.UsuarioResponse;
import br.com.inovadados.teacherplatform.exception.AccountInactiveException;
import br.com.inovadados.teacherplatform.exception.AccountLockedException;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.SessaoRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final JwtService jwtService;
    private final RateLimitService rateLimitService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.access-token-expiration}")
    private int accessTokenExpiration;

    @Value("${app.auth.max-attempts}")
    private int maxAttempts;

    @Transactional
    public AuthLoginResult login(LoginRequest request, HttpServletRequest httpRequest) {
        Usuario usuario = getUsuario(request);

        checkRateLimit(request);

        checkCredentials(request, usuario);

        if (!usuario.getAtivo()) {
            throw new AccountInactiveException();
        }

        String accessToken = jwtService.generateAccessToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        Sessao sessao = buildSession(httpRequest, usuario, refreshToken);

        sessaoRepository.save(sessao);

        rateLimitService.clearAttempts(request.email());

        usuario.setUltimoAcesso(OffsetDateTime.now(ZoneOffset.UTC));

        LoginResponse loginResponse = new LoginResponse(
            accessToken,
            accessTokenExpiration,
            usuario.getPerfil().name(),
            new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getAvatarUrl())
        );

        return new AuthLoginResult(loginResponse, refreshToken);
    }

    private Sessao buildSession(HttpServletRequest httpRequest, Usuario usuario, String refreshToken) {
        Sessao sessao = new Sessao();
        sessao.setUsuario(usuario);
        sessao.setRefreshTokenHash(hashSha256(refreshToken));
        sessao.setUserAgent(httpRequest.getHeader("User-Agent"));
        sessao.setIp(getClientIp(httpRequest));
        sessao.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        sessao.setExpiraEm(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(jwtService.getRefreshTokenExpiration()));
        return sessao;
    }

    private void checkCredentials(LoginRequest request, Usuario usuario) {
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            long attempts = rateLimitService.incrementAttempts(request.email());
            if (attempts >= maxAttempts) {
                rateLimitService.lockAccount(request.email());
                Instant expiry = rateLimitService.getLockExpiry(request.email())
                    .orElse(Instant.now().plusSeconds(15 * 60L));
                throw new AccountLockedException(expiry);
            }
            throw new BadCredentialsException("Credenciais inválidas");
        }
    }

    private void checkRateLimit(LoginRequest request) {
        Optional<Instant> lockExpiry = rateLimitService.getLockExpiry(request.email());
        if (lockExpiry.isPresent()) {
            throw new AccountLockedException(lockExpiry.get());
        }
    }

    private Usuario getUsuario(LoginRequest request) {
        Optional<Usuario> opt = usuarioRepository.findByEmail(request.email());
        if (opt.isEmpty()) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        return opt.get();
    }

    private static final int MAX_SESSOES_ATIVAS = 5;

    @Transactional
    public AuthLoginResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token ausente");
        }

        if (!jwtService.isTokenValid(rawRefreshToken)) {
            throw new UnauthorizedException("Refresh token inválido ou expirado");
        }

        String hash = hashSha256(rawRefreshToken);
        Sessao sessaoAtual = sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(hash)
            .orElseThrow(() -> new UnauthorizedException("Sessão não encontrada ou revogada"));

        Usuario usuario = sessaoAtual.getUsuario();

        List<Sessao> sessoesAtivas = sessaoRepository
            .findByUsuarioIdAndRevogadoEmIsNullOrderByCriadoEmAsc(usuario.getId());

        // TODO create method checkMaxSessions
        if (sessoesAtivas.size() >= MAX_SESSOES_ATIVAS) {
            Sessao maisAntiga = sessoesAtivas.get(0);
            if (!maisAntiga.getId().equals(sessaoAtual.getId())) {
                maisAntiga.setRevogadoEm(OffsetDateTime.now(ZoneOffset.UTC));
                sessaoRepository.save(maisAntiga);
            }
        }

        sessaoAtual.setRevogadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        sessaoRepository.save(sessaoAtual);

        String novoAccessToken = jwtService.generateAccessToken(usuario);
        String novoRefreshToken = jwtService.generateRefreshToken(usuario);

        // TODO usar build session
        Sessao novaSessao = new Sessao();
        novaSessao.setUsuario(usuario);
        novaSessao.setRefreshTokenHash(hashSha256(novoRefreshToken));
        novaSessao.setUserAgent(sessaoAtual.getUserAgent());
        novaSessao.setIp(sessaoAtual.getIp());
        novaSessao.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        novaSessao.setExpiraEm(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(jwtService.getRefreshTokenExpiration()));
        sessaoRepository.save(novaSessao);

        LoginResponse loginResponse = new LoginResponse(
            novoAccessToken,
            accessTokenExpiration,
            usuario.getPerfil().name(),
            new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getAvatarUrl())
        );

        return new AuthLoginResult(loginResponse, novoRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String hash = hashSha256(rawRefreshToken);
            sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(hash).ifPresent(sessao -> {
                sessao.setRevogadoEm(OffsetDateTime.now(ZoneOffset.UTC));
                sessaoRepository.save(sessao);
            });
        }
    }

    // TODO Extract to hash utils
    private String hashSha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
