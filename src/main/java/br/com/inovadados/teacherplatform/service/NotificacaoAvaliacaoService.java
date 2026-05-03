package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.event.AvaliacaoPublicadaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificacaoAvaliacaoService {

    @EventListener
    public void onAvaliacaoPublicada(AvaliacaoPublicadaEvent event) {
        log.info("Avaliação {} publicada para {} turmas — notificações pendentes de implementação",
                event.avaliacaoId(), event.turmasIds().size());
    }
}
