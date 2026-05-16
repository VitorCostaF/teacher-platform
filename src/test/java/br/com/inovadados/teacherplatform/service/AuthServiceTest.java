package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Sessao;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import br.com.inovadados.teacherplatform.dto.request.LoginRequest;
import br.com.inovadados.teacherplatform.exception.AccountInactiveException;
import br.com.inovadados.teacherplatform.exception.AccountLockedException;
import br.com.inovadados.teacherplatform.exception.UnauthorizedException;
import br.com.inovadados.teacherplatform.repository.SessaoRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    SessaoRepository sessaoRepository;

    @Mock
    JwtService jwtService;

    @Mock
    RateLimitService rateLimitService;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 900);
        ReflectionTestUtils.setField(authService, "maxAttempts", 5);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("professor@escola.com");
        usuario.setSenhaHash("$2a$hashed");
        usuario.setPerfil(PerfilEnum.PROFESSOR);
        usuario.setNome("Prof Silva");
        usuario.setAtivo(true);

        loginRequest = new LoginRequest("professor@escola.com", "senha123");
        httpRequest = mock(HttpServletRequest.class);
    }

    // --- Caminho feliz ---

    @Test
    void login_caminhoFeliz_deveRetornarAuthLoginResult() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        AuthLoginResult resultado = authService.login(loginRequest, httpRequest);

        assertNotNull(resultado);
        assertNotNull(resultado.loginResponse());
        assertEquals("refresh-token", resultado.refreshToken());
    }

    @Test
    void login_caminhoFeliz_deveGerarAccessTokenERefreshToken() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        authService.login(loginRequest, httpRequest);

        verify(jwtService, times(1)).generateAccessToken(usuario);
        verify(jwtService, times(1)).generateRefreshToken(usuario);
    }

    @Test
    void login_caminhoFeliz_deveSalvarSessao() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        authService.login(loginRequest, httpRequest);

        verify(sessaoRepository, times(1)).save(any(Sessao.class));
    }

    @Test
    void login_caminhoFeliz_deveLimparTentativasAposLogin() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        authService.login(loginRequest, httpRequest);

        verify(rateLimitService, times(1)).clearAttempts(loginRequest.email());
    }

    @Test
    void login_caminhoFeliz_devePreencherUltimoAcesso() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        OffsetDateTime antes = OffsetDateTime.now().minusSeconds(1);
        authService.login(loginRequest, httpRequest);

        assertNotNull(usuario.getUltimoAcesso());
        assertTrue(usuario.getUltimoAcesso().isAfter(antes));
    }

    @Test
    void login_caminhoFeliz_loginResponseDeveConterPerfilDoUsuario() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        AuthLoginResult resultado = authService.login(loginRequest, httpRequest);

        assertEquals(PerfilEnum.PROFESSOR.name(), resultado.loginResponse().perfil());
    }

    // --- Erros de autenticação ---

    @Test
    void login_emailNaoCadastrado_deveLancarBadCredentialsException() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, httpRequest));
    }

    @Test
    void login_senhaErradaAbaixoDoLimite_deveLancarBadCredentialsEIncrementarTentativas() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(false);
        when(rateLimitService.incrementAttempts(loginRequest.email())).thenReturn(2L);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, httpRequest));
        verify(rateLimitService, times(1)).incrementAttempts(loginRequest.email());
    }

    @Test
    void login_senhaErradaAtingindoLimite_deveLancarAccountLockedEBloquearConta() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(false);
        when(rateLimitService.incrementAttempts(loginRequest.email())).thenReturn(5L);
        when(rateLimitService.getLockExpiry(loginRequest.email()))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(Instant.now().plusSeconds(900)));

        assertThrows(AccountLockedException.class, () -> authService.login(loginRequest, httpRequest));
        verify(rateLimitService, times(1)).lockAccount(loginRequest.email());
    }

    @Test
    void login_contaBloqueada_deveLancarAccountLockedSemVerificarSenha() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email()))
            .thenReturn(Optional.of(Instant.now().plusSeconds(900)));

        assertThrows(AccountLockedException.class, () -> authService.login(loginRequest, httpRequest));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_usuarioInativo_deveLancarAccountInactiveException() {
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);

        assertThrows(AccountInactiveException.class, () -> authService.login(loginRequest, httpRequest));
    }

    // --- Construção da sessão ---

    @Test
    void login_sessao_refreshTokenHashDeveSerSha256DoRefreshToken() throws Exception {
        String refreshToken = "meu-refresh-token-gerado";
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn(refreshToken);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        authService.login(loginRequest, httpRequest);

        String hashEsperado = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(refreshToken.getBytes(StandardCharsets.UTF_8))
        );

        ArgumentCaptor<Sessao> captor = ArgumentCaptor.forClass(Sessao.class);
        verify(sessaoRepository).save(captor.capture());
        assertEquals(hashEsperado, captor.getValue().getRefreshTokenHash());
    }

    @Test
    void login_sessao_ipDeveVirDoXForwardedForQuandoPresente() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 10.0.0.1");

        authService.login(loginRequest, httpRequest);

        ArgumentCaptor<Sessao> captor = ArgumentCaptor.forClass(Sessao.class);
        verify(sessaoRepository).save(captor.capture());
        assertEquals("203.0.113.1", captor.getValue().getIp());
    }

    @Test
    void login_sessao_ipDeveVirDoRemoteAddrQuandoXForwardedForAusente() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("10.0.0.5");

        authService.login(loginRequest, httpRequest);

        ArgumentCaptor<Sessao> captor = ArgumentCaptor.forClass(Sessao.class);
        verify(sessaoRepository).save(captor.capture());
        assertEquals("10.0.0.5", captor.getValue().getIp());
    }

    @Test
    void login_sessao_expiraEmDeveSerAproximadamenteNowMaisRefreshExpiration() {
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuario));
        when(rateLimitService.getLockExpiry(loginRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginRequest.senha(), usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.generateAccessToken(usuario)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("refresh-token");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        OffsetDateTime antes = OffsetDateTime.now().plusSeconds(2592000L - 5);
        authService.login(loginRequest, httpRequest);
        OffsetDateTime depois = OffsetDateTime.now().plusSeconds(2592000L + 5);

        ArgumentCaptor<Sessao> captor = ArgumentCaptor.forClass(Sessao.class);
        verify(sessaoRepository).save(captor.capture());
        OffsetDateTime expiraEm = captor.getValue().getExpiraEm();
        assertTrue(expiraEm.isAfter(antes) && expiraEm.isBefore(depois));
    }

    // --- refresh ---

    private Sessao sessaoComHash(String rawToken) throws Exception {
        String hash = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8))
        );
        Sessao s = new Sessao();
        s.setId(99L);
        s.setUsuario(usuario);
        s.setRefreshTokenHash(hash);
        s.setIp("127.0.0.1");
        s.setUserAgent("Mozilla");
        s.setCriadoEm(OffsetDateTime.now());
        s.setExpiraEm(OffsetDateTime.now().plusDays(30));
        return s;
    }

    private List<Sessao> sessoesAtivas(int quantidade, Sessao sessaoAtual) {
        List<Sessao> lista = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Sessao s = new Sessao();
            s.setId((long) i);
            lista.add(s);
        }
        lista.add(sessaoAtual);
        return lista;
    }

    @Test
    void refresh_tokenNull_deveLancarUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> authService.refresh(null));
    }

    @Test
    void refresh_tokenVazio_deveLancarUnauthorizedException() {
        assertThrows(UnauthorizedException.class, () -> authService.refresh(""));
    }

    @Test
    void refresh_tokenInvalido_deveLancarUnauthorizedException() {
        when(jwtService.isTokenValid("token-invalido")).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> authService.refresh("token-invalido"));
    }

    @Test
    void refresh_sessaoNaoEncontrada_deveLancarUnauthorizedException() {
        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(any())).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> authService.refresh("refresh-token"));
    }

    @Test
    void refresh_bemSucedido_deveRevogarSessaoAtualECriarNova() throws Exception {
        String rawToken = "refresh-token-valido";
        Sessao sessaoAtual = sessaoComHash(rawToken);
        when(jwtService.isTokenValid(rawToken)).thenReturn(true);
        when(sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(any())).thenReturn(Optional.of(sessaoAtual));
        when(sessaoRepository.findByUsuarioIdAndRevogadoEmIsNullOrderByCriadoEmAsc(usuario.getId()))
            .thenReturn(List.of(sessaoAtual));
        when(jwtService.generateAccessToken(usuario)).thenReturn("novo-access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("novo-refresh");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);

        authService.refresh(rawToken);

        verify(sessaoRepository, times(2)).save(any(Sessao.class));
        assertNotNull(sessaoAtual.getRevogadoEm());
    }

    @Test
    void refresh_bemSucedido_deveGerarNovosTokens() throws Exception {
        String rawToken = "refresh-token-valido";
        Sessao sessaoAtual = sessaoComHash(rawToken);
        when(jwtService.isTokenValid(rawToken)).thenReturn(true);
        when(sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(any())).thenReturn(Optional.of(sessaoAtual));
        when(sessaoRepository.findByUsuarioIdAndRevogadoEmIsNullOrderByCriadoEmAsc(usuario.getId()))
            .thenReturn(List.of(sessaoAtual));
        when(jwtService.generateAccessToken(usuario)).thenReturn("novo-access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("novo-refresh");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);

        AuthLoginResult resultado = authService.refresh(rawToken);

        verify(jwtService).generateAccessToken(usuario);
        verify(jwtService).generateRefreshToken(usuario);
        assertEquals("novo-refresh", resultado.refreshToken());
    }

    @Test
    void refresh_com5SessoesEMaisAntigaDiferenteDaAtual_deveRevogarMaisAntiga() throws Exception {
        String rawToken = "refresh-token-valido";
        Sessao sessaoAtual = sessaoComHash(rawToken);
        sessaoAtual.setId(99L);

        List<Sessao> sessoes = sessoesAtivas(5, sessaoAtual);
        Sessao maisAntiga = sessoes.get(0);

        when(jwtService.isTokenValid(rawToken)).thenReturn(true);
        when(sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(any())).thenReturn(Optional.of(sessaoAtual));
        when(sessaoRepository.findByUsuarioIdAndRevogadoEmIsNullOrderByCriadoEmAsc(usuario.getId()))
            .thenReturn(sessoes);
        when(jwtService.generateAccessToken(usuario)).thenReturn("novo-access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("novo-refresh");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);

        authService.refresh(rawToken);

        assertNotNull(maisAntiga.getRevogadoEm());
    }

    @Test
    void refresh_com5SessoesEMaisAntigaIgualAtual_naoDeveRevogarMaisAntiga() throws Exception {
        String rawToken = "refresh-token-valido";
        Sessao sessaoAtual = sessaoComHash(rawToken);
        sessaoAtual.setId(0L);

        List<Sessao> sessoes = sessoesAtivas(4, sessaoAtual);
        sessoes.add(0, sessaoAtual);

        when(jwtService.isTokenValid(rawToken)).thenReturn(true);
        when(sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(any())).thenReturn(Optional.of(sessaoAtual));
        when(sessaoRepository.findByUsuarioIdAndRevogadoEmIsNullOrderByCriadoEmAsc(usuario.getId()))
            .thenReturn(sessoes);
        when(jwtService.generateAccessToken(usuario)).thenReturn("novo-access");
        when(jwtService.generateRefreshToken(usuario)).thenReturn("novo-refresh");
        when(jwtService.getRefreshTokenExpiration()).thenReturn(2592000L);

        authService.refresh(rawToken);

        verify(sessaoRepository, times(2)).save(any(Sessao.class));
    }

    // --- logout ---

    @Test
    void logout_tokenNull_naoDeveLancarExcecaoNemChamarSave() {
        assertDoesNotThrow(() -> authService.logout(null));
        verify(sessaoRepository, never()).save(any());
    }

    @Test
    void logout_tokenVazio_naoDeveLancarExcecaoNemChamarSave() {
        assertDoesNotThrow(() -> authService.logout(""));
        verify(sessaoRepository, never()).save(any());
    }

    @Test
    void logout_sessaoEncontrada_devePreencherRevogadoEmEChamarSave() {
        Sessao sessao = new Sessao();
        sessao.setId(1L);
        when(sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(any())).thenReturn(Optional.of(sessao));

        authService.logout("refresh-token");

        assertNotNull(sessao.getRevogadoEm());
        verify(sessaoRepository, times(1)).save(sessao);
    }

    @Test
    void logout_sessaoNaoEncontrada_naoDeveLancarExcecao() {
        when(sessaoRepository.findByRefreshTokenHashAndRevogadoEmIsNull(any())).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> authService.logout("refresh-token"));
    }
}
