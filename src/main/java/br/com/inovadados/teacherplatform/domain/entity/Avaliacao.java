package br.com.inovadados.teacherplatform.domain.entity;

import br.com.inovadados.teacherplatform.domain.enums.GabaritoLiberacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.StatusAvaliacaoEnum;
import br.com.inovadados.teacherplatform.domain.enums.TipoAvaliacaoEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Usuario professor;

    @Column(nullable = false)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAvaliacaoEnum tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAvaliacaoEnum status;

    @Column(name = "peso_nota", precision = 4, scale = 2)
    private BigDecimal pesoNota;

    @Column(name = "duracao_minutos")
    private Integer duracaoMinutos;

    @Column(name = "disponivel_em")
    private OffsetDateTime disponivelEm;

    @Column(name = "encerra_em")
    private OffsetDateTime encerraEm;

    @Column(name = "embaralhar_questoes", nullable = false)
    private boolean embaralharQuestoes;

    @Column(name = "embaralhar_alternativas", nullable = false)
    private boolean embaralharAlternativas;

    @Enumerated(EnumType.STRING)
    @Column(name = "gabarito_liberacao", nullable = false)
    private GabaritoLiberacaoEnum gabaritoLiberacao;

    @Column(name = "permite_entrega_atrasada", nullable = false)
    private boolean permiteEntregaAtrasada;

    @Column(name = "gerado_por_ia", nullable = false)
    private boolean geradoPorIa;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @OneToMany(mappedBy = "avaliacao")
    private List<Questao> questoes;
}
