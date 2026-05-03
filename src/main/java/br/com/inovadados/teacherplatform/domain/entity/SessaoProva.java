package br.com.inovadados.teacherplatform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "sessoes_prova")
public class SessaoProva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @Column(name = "iniciada_em", nullable = false)
    private OffsetDateTime iniciadaEm;

    @Column(name = "encerrada_em")
    private OffsetDateTime encerradaEm;

    @Column(name = "entregue_manualmente", nullable = false)
    private boolean entregueManualmente;
}
