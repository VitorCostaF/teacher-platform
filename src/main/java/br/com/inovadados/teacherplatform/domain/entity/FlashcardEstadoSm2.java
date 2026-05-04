package br.com.inovadados.teacherplatform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "flashcard_estado_sm2")
public class FlashcardEstadoSm2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Column(name = "intervalo_dias", nullable = false)
    private Integer intervaloDias = 1;

    @Column(name = "fator_facilidade", nullable = false, precision = 4, scale = 2)
    private BigDecimal fatorFacilidade = new BigDecimal("2.5");

    @Column(name = "proxima_revisao", nullable = false)
    private LocalDate proximaRevisao = LocalDate.now();

    @Column(name = "total_revisoes", nullable = false)
    private Integer totalRevisoes = 0;
}
