package br.com.inovadados.teacherplatform.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "observacoes_professor")
public class ObservacaoProfessor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Column(name = "professor_id", nullable = false)
    private UUID professorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;
}
