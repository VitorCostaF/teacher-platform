package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.TokenTemporario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenTemporarioRepository extends JpaRepository<TokenTemporario, Long> {

    Optional<TokenTemporario> findByTokenHashAndUsadoEmIsNull(String hash);
}
