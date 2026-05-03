package br.com.inovadados.teacherplatform.domain.entity;

import br.com.inovadados.teacherplatform.domain.enums.TipoConteudoEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "conteudos")
public class Conteudo {

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
    private TipoConteudoEnum tipo;

    @Column(columnDefinition = "TEXT")
    private String corpo;

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "link_externo", length = 500)
    private String linkExterno;

    @Column(name = "publicado_em")
    private OffsetDateTime publicadoEm;

    @Column(name = "serie_sugerida", length = 50)
    private String serieSugerida;

    @Column(columnDefinition = "text[]")
    private String[] topicos;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;
}
