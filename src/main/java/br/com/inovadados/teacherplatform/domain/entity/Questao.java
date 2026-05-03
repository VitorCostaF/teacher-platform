package br.com.inovadados.teacherplatform.domain.entity;

import br.com.inovadados.teacherplatform.domain.enums.TipoQuestaoEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "questoes")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @Column(nullable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoQuestaoEnum tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String alternativas;

    @Column(name = "gabarito_dissertativo", columnDefinition = "TEXT")
    private String gabaritoDissertativo;

    @Column(name = "gabarito_indice")
    private Integer gabaritoIndice;

    @Column(length = 20)
    private String dificuldade;

    @Column(length = 150)
    private String topico;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal pontos;
}
