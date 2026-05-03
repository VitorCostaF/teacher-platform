package br.com.inovadados.teacherplatform.domain.entity;

import br.com.inovadados.teacherplatform.domain.enums.StatusFrequenciaEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "registros_frequencia")
public class RegistroFrequencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @Column(name = "data_aula", nullable = false)
    private LocalDate dataAula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFrequenciaEnum status;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lancado_por", nullable = false)
    private Usuario lancadoPor;

    @Column(name = "lancado_em", nullable = false)
    private OffsetDateTime lancadoEm;

    @Column(name = "editado_em")
    private OffsetDateTime editadoEm;
}
