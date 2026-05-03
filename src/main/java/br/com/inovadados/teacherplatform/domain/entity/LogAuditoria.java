package br.com.inovadados.teacherplatform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;

@Getter
@Setter
@Immutable
@Entity
@Table(name = "logs_auditoria")
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escola_id", nullable = false)
    private Escola escola;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String acao;

    @Column(nullable = false, length = 100)
    private String entidade;

    @Column(name = "entidade_id", length = 255)
    private String entidadeId;

    @Column(name = "dados_anteriores", columnDefinition = "jsonb")
    private String dadosAnteriores;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(length = 45)
    private String ip;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;
}
