package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.Conquista;
import br.com.inovadados.teacherplatform.domain.entity.Entrega;
import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import br.com.inovadados.teacherplatform.dto.response.ConquistaDto;
import br.com.inovadados.teacherplatform.repository.ConquistaRepository;
import br.com.inovadados.teacherplatform.repository.EntregaRepository;
import br.com.inovadados.teacherplatform.repository.FlashcardEstadoSm2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GamificacaoService {

    private final EntregaRepository entregaRepository;
    private final FlashcardEstadoSm2Repository estadoSm2Repository;
    private final ConquistaRepository conquistaRepository;

    public int calcularPontos(UUID alunoId) {
        List<Entrega> entregas = entregaRepository.findByAlunoId(alunoId);
        int pontos = 0;

        for (Entrega e : entregas) {
            if (e.getStatus() != StatusEntregaEnum.ENTREGUE && e.getStatus() != StatusEntregaEnum.CORRIGIDA) {
                continue;
            }
            pontos += e.isEntregaAtrasada() ? 3 : 10;
            if (e.getNotaFinal() != null && e.getNotaFinal().compareTo(new BigDecimal("7")) >= 0) {
                pontos += 20;
            }
        }

        int flashcardsRevisados = estadoSm2Repository.findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(
                        alunoId, LocalDate.now().plusYears(10))
                .stream()
                .mapToInt(e -> e.getTotalRevisoes())
                .sum();
        pontos += flashcardsRevisados;

        return pontos;
    }

    public void verificarConquistas(UUID alunoId) {
        List<Entrega> entregas = entregaRepository.findByAlunoId(alunoId);

        long entregasNoPrazo = entregas.stream()
                .filter(e -> e.getStatus() == StatusEntregaEnum.ENTREGUE || e.getStatus() == StatusEntregaEnum.CORRIGIDA)
                .filter(e -> !e.isEntregaAtrasada())
                .count();

        if (entregasNoPrazo >= 10 && !conquistaRepository.existsByAlunoIdAndTipo(alunoId, "DEDICADO")) {
            salvarConquista(alunoId, "DEDICADO", "10 entregas realizadas no prazo");
        }

        long notasAltas = entregas.stream()
                .filter(e -> e.getNotaFinal() != null)
                .filter(e -> e.getNotaFinal().compareTo(new BigDecimal("9")) >= 0)
                .count();

        if (notasAltas >= 5 && !conquistaRepository.existsByAlunoIdAndTipo(alunoId, "EXCELENCIA")) {
            salvarConquista(alunoId, "EXCELENCIA", "5 avaliações com nota acima de 9");
        }

        int totalFlashcards = estadoSm2Repository
                .findByAlunoIdAndProximaRevisaoLessThanEqualOrderByProximaRevisaoAsc(alunoId, LocalDate.now().plusYears(10))
                .stream()
                .mapToInt(e -> e.getTotalRevisoes())
                .sum();

        if (totalFlashcards >= 50 && !conquistaRepository.existsByAlunoIdAndTipo(alunoId, "FLASHMASTER")) {
            salvarConquista(alunoId, "FLASHMASTER", "50 flashcards revisados");
        }
    }

    @Transactional(readOnly = true)
    public List<ConquistaDto> getConquistas(UUID alunoId) {
        return conquistaRepository.findByAlunoIdOrderByObtidaEmDesc(alunoId)
                .stream()
                .map(c -> new ConquistaDto(c.getId(), c.getTipo(), c.getDescricao(), c.getObtidaEm()))
                .toList();
    }

    private void salvarConquista(UUID alunoId, String tipo, String descricao) {
        var conquista = new Conquista();
        conquista.setAlunoId(alunoId);
        conquista.setTipo(tipo);
        conquista.setDescricao(descricao);
        conquista.setObtidaEm(OffsetDateTime.now());
        conquistaRepository.save(conquista);
    }
}
