package br.com.inovadados.teacherplatform.domain.entity;

import br.com.inovadados.teacherplatform.domain.enums.SistemaAvaliacaoEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "escolas")
public class Escola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "nota_minima_aprovacao", nullable = false, precision = 4, scale = 2)
    private BigDecimal notaMinimaAprovacao;

    @Column(name = "frequencia_minima_aprovacao", nullable = false, precision = 5, scale = 2)
    private BigDecimal frequenciaMinimaAprovacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "sistema_avaliacao", nullable = false, length = 30)
    private SistemaAvaliacaoEnum sistemaAvaliacao;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @OneToMany(mappedBy = "escola")
    private List<Usuario> usuarios;
}
