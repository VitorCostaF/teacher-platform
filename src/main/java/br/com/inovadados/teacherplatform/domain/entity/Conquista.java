package br.com.inovadados.teacherplatform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "conquistas")
public class Conquista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(name = "obtida_em", nullable = false)
    private OffsetDateTime obtidaEm;
}
