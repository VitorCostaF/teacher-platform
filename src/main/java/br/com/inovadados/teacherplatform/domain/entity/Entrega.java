package br.com.inovadados.teacherplatform.domain.entity;

import br.com.inovadados.teacherplatform.domain.enums.StatusEntregaEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "entregas")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Usuario aluno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEntregaEnum status;

    @Column(name = "iniciado_em")
    private OffsetDateTime iniciadoEm;

    @Column(name = "entregue_em")
    private OffsetDateTime entregueEm;

    @Column(name = "nota_automatica", precision = 5, scale = 2)
    private BigDecimal notaAutomatica;

    @Column(name = "nota_final", precision = 5, scale = 2)
    private BigDecimal notaFinal;

    @Column(name = "entrega_atrasada", nullable = false)
    private boolean entregaAtrasada;

    @OneToMany(mappedBy = "entrega")
    private List<Resposta> respostas;
}
