package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "dGVzdFNlY3JldEtleUZvckp3dFNlcnZpY2VUZXN0aW5nMTIz";
    private static final long ACCESS_EXPIRATION = 900L;
    private static final long REFRESH_EXPIRATION = 2592000L;

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_EXPIRATION);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("professor@escola.com");
        usuario.setPerfil(PerfilEnum.PROFESSOR);
    }

    // --- generateAccessToken ---

    @Test
    void generateAccessToken_deveConterSubjectComoEmail() {
        String token = jwtService.generateAccessToken(usuario);
        String email = jwtService.extractEmail(token);
        assertEquals(usuario.getEmail(), email);
    }

    @Test
    void generateAccessToken_deveConterClaimPerfil() {
        String token = jwtService.generateAccessToken(usuario);
        Claims claims = parseClaims(token);
        assertEquals(PerfilEnum.PROFESSOR.name(), claims.get("perfil", String.class));
    }

    @Test
    void generateAccessToken_deveConterClaimUsuarioId() {
        String token = jwtService.generateAccessToken(usuario);
        Claims claims = parseClaims(token);
        assertEquals(usuario.getId().toString(), claims.get("usuarioId", String.class));
    }

    @Test
    void generateAccessToken_deveSerValidoAposGeracao() {
        String token = jwtService.generateAccessToken(usuario);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void generateAccessToken_deveExpirarComExpiracaoNegativa() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1L);
        String token = jwtService.generateAccessToken(usuario);
        assertFalse(jwtService.isTokenValid(token));
    }

    // --- generateRefreshToken ---

    @Test
    void generateRefreshToken_deveGerarTokensDiferentesACadaChamada() {
        String token1 = jwtService.generateRefreshToken(usuario);
        String token2 = jwtService.generateRefreshToken(usuario);
        assertNotEquals(token1, token2);
    }

    @Test
    void generateRefreshToken_naoDeveConterClaimPerfil() {
        String token = jwtService.generateRefreshToken(usuario);
        Claims claims = parseClaims(token);
        assertNull(claims.get("perfil"));
    }

    @Test
    void generateRefreshToken_deveConterSubjectComoEmail() {
        String token = jwtService.generateRefreshToken(usuario);
        assertEquals(usuario.getEmail(), jwtService.extractEmail(token));
    }

    @Test
    void generateRefreshToken_deveSerValidoAposGeracao() {
        String token = jwtService.generateRefreshToken(usuario);
        assertTrue(jwtService.isTokenValid(token));
    }

    // --- extractEmail ---

    @Test
    void extractEmail_deveRetornarEmailCorretoDeAccessToken() {
        String token = jwtService.generateAccessToken(usuario);
        assertEquals(usuario.getEmail(), jwtService.extractEmail(token));
    }

    @Test
    void extractEmail_deveRetornarEmailCorretoDeRefreshToken() {
        String token = jwtService.generateRefreshToken(usuario);
        assertEquals(usuario.getEmail(), jwtService.extractEmail(token));
    }

    @Test
    void extractEmail_deveLancarExcecaoParaTokenComAssinaturaAdulterada() {
        String token = jwtService.generateAccessToken(usuario);
        String tokenAdulterado = token.substring(0, token.lastIndexOf('.') + 1) + "assinaturaErrada";
        assertThrows(Exception.class, () -> jwtService.extractEmail(tokenAdulterado));
    }

    @Test
    void extractEmail_deveLancarExcecaoParaTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1L);
        String token = jwtService.generateAccessToken(usuario);
        assertThrows(Exception.class, () -> jwtService.extractEmail(token));
    }

    // --- isTokenValid ---

    @Test
    void isTokenValid_deveRetornarFalseParaTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1L);
        String token = jwtService.generateAccessToken(usuario);
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_deveRetornarFalseParaAssinaturaErrada() {
        String token = jwtService.generateAccessToken(usuario);
        String tokenAdulterado = token.substring(0, token.lastIndexOf('.') + 1) + "assinaturaErrada";
        assertFalse(jwtService.isTokenValid(tokenAdulterado));
    }

    @Test
    void isTokenValid_deveRetornarFalseParaStringVazia() {
        assertFalse(jwtService.isTokenValid(""));
    }

    @Test
    void isTokenValid_deveRetornarFalseParaNull() {
        assertFalse(jwtService.isTokenValid(null));
    }

    @Test
    void isTokenValid_deveRetornarFalseParaTokenMalformado() {
        assertFalse(jwtService.isTokenValid("token.invalido.qualquer"));
    }

    @Test
    void isTokenValid_nuncaDeveLancarExcecao() {
        assertDoesNotThrow(() -> jwtService.isTokenValid(null));
        assertDoesNotThrow(() -> jwtService.isTokenValid(""));
        assertDoesNotThrow(() -> jwtService.isTokenValid("lixo"));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
