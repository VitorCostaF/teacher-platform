package br.com.inovadados.teacherplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AlertaFrequenciaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaFrequenciaService.class);

    @Async
    public void verificarAlertas(Long turmaId, UUID alunoId, double percentual, boolean tresFaltasConsecutivas) {
        if (percentual < 75.0) {
            log.info("Alerta frequência: aluno {} turma {} — {}% de presença", alunoId, turmaId, String.format("%.1f", percentual));
        }
        if (tresFaltasConsecutivas) {
            log.info("Alerta frequência: aluno {} turma {} — 3 faltas consecutivas", alunoId, turmaId);
        }
    }
}
