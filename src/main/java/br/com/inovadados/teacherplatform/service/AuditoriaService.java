package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.LogAuditoria;
import br.com.inovadados.teacherplatform.repository.EscolaRepository;
import br.com.inovadados.teacherplatform.repository.LogAuditoriaRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EscolaRepository escolaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void registrar(Long escolaId, UUID usuarioId, String acao, String entidade,
                          String entidadeId, Object dadosAnteriores, Object dadosNovos,
                          String motivo, HttpServletRequest request) {
        LogAuditoria log = new LogAuditoria();
        log.setEscola(escolaRepository.getReferenceById(escolaId));
        if (usuarioId != null) {
            log.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        }
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setDadosAnteriores(toJson(dadosAnteriores));
        log.setDadosNovos(toJson(dadosNovos));
        log.setMotivo(motivo);
        log.setIp(extrairIp(request));
        log.setCriadoEm(OffsetDateTime.now(ZoneOffset.UTC));
        logAuditoriaRepository.save(log);
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String extrairIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
