package br.com.inovadados.teacherplatform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "respostas")
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_id", nullable = false)
    private Entrega entrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Column(name = "resposta_indice")
    private Integer respostaIndice;

    @Column(name = "resposta_texto", columnDefinition = "TEXT")
    private String respostaTexto;

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    private Boolean correta;

    @Column(name = "nota_manual", precision = 5, scale = 2)
    private BigDecimal notaManual;
}
