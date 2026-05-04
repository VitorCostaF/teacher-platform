package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.SessaoProva;
import br.com.inovadados.teacherplatform.repository.SessaoProvaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpiracaoSessaoJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoSessaoJob.class);

    private final SessaoProvaRepository sessaoProvaRepository;
    private final SessaoProvaService sessaoProvaService;

    @Scheduled(fixedRate = 60000)
    public void expirarSessoesVencidas() {
        List<SessaoProva> expiradas = sessaoProvaRepository
                .findByEncerradaEmIsNullAndAvaliacaoEncerraEmBefore(OffsetDateTime.now());

        if (expiradas.isEmpty()) return;

        log.info("Expirando {} sessões de prova", expiradas.size());
        for (SessaoProva sessao : expiradas) {
            log.info("Encerrando sessão {} do aluno {}", sessao.getId(), sessao.getAluno().getId());
            sessaoProvaService.encerrarPorExpiracao(sessao);
        }
    }
}
