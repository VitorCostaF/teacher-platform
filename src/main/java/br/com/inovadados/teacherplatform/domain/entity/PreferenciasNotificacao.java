package br.com.inovadados.teacherplatform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "preferencias_notificacao")
public class PreferenciasNotificacao {

    @Id
    @Column(name = "usuario_id")
    private UUID usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "falta_aluno", nullable = false)
    private boolean faltaAluno = true;

    @Column(name = "queda_frequencia", nullable = false)
    private boolean quedaFrequencia = true;

    @Column(name = "prazo_prova", nullable = false)
    private boolean prazoProva = true;
}
