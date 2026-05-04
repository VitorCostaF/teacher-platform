package br.com.inovadados.teacherplatform.repository;

import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByEscolaIdAndPerfil(Long escolaId, PerfilEnum perfil);

    long countByEscolaIdAndPerfil(Long escolaId, PerfilEnum perfil);

    @Query("SELECT u FROM Usuario u WHERE u.escola.id = :escolaId AND u.perfil = :perfil " +
           "AND (:nome IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:ativo IS NULL OR u.ativo = :ativo)")
    Page<Usuario> buscarPorEscolaEPerfil(@Param("escolaId") Long escolaId,
                                          @Param("perfil") PerfilEnum perfil,
                                          @Param("nome") String nome,
                                          @Param("ativo") Boolean ativo,
                                          Pageable pageable);
}
