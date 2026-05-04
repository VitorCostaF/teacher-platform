package br.com.inovadados.teacherplatform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/professor/**").hasAnyRole("PROFESSOR", "ADMIN", "COORDENADOR")
                .requestMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("ADMIN", "COORDENADOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/ia/**").hasAnyRole("PROFESSOR", "ADMIN", "COORDENADOR")
                .requestMatchers("/upload/**").hasAnyRole("PROFESSOR", "ADMIN", "COORDENADOR")
                // Rotas de atividade/prova acessíveis ao aluno (avaliadas antes das catch-alls)
                .requestMatchers(HttpMethod.GET, "/atividades/*").hasRole("ALUNO")
                .requestMatchers(HttpMethod.PUT, "/atividades/*/rascunho").hasRole("ALUNO")
                .requestMatchers(HttpMethod.POST, "/atividades/*/entregar").hasRole("ALUNO")
                .requestMatchers(HttpMethod.POST, "/provas/*/iniciar").hasRole("ALUNO")
                .requestMatchers(HttpMethod.PUT, "/provas/*/sessoes/*/autosave").hasRole("ALUNO")
                .requestMatchers(HttpMethod.POST, "/provas/*/sessoes/*/entregar").hasRole("ALUNO")
                // Catch-alls para professor
                .requestMatchers("/provas/**").hasAnyRole("PROFESSOR", "ADMIN", "COORDENADOR")
                .requestMatchers("/atividades/**").hasAnyRole("PROFESSOR", "ADMIN", "COORDENADOR")
                .requestMatchers("/relatorios/**").hasAnyRole("PROFESSOR", "ADMIN", "COORDENADOR")
                .requestMatchers("/aluno/**").hasRole("ALUNO")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
