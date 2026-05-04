package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.enums.PerfilEnum;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoAlunoResponse;
import br.com.inovadados.teacherplatform.dto.response.DesempenhoTurmaResponse;
import br.com.inovadados.teacherplatform.dto.response.RelatorioStatusResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private static final Logger log = LoggerFactory.getLogger(RelatorioService.class);

    private final DashboardService dashboardService;

    @Value("${app.storage.pdf-dir:./uploads/relatorios}")
    private String pdfDir;

    @Value("${app.storage.pdf-base-url:http://localhost:8080/relatorios}")
    private String pdfBaseUrl;

    private final ConcurrentHashMap<String, RelatorioStatusResponse> statusMap = new ConcurrentHashMap<>();

    public RelatorioStatusResponse solicitarRelatorioPDF(String tipo, Long referenciaId, UUID solicitanteId) {
        String jobId = UUID.randomUUID().toString();
        RelatorioStatusResponse status = new RelatorioStatusResponse(jobId, "PROCESSANDO", null);
        statusMap.put(jobId, status);

        if ("TURMA".equals(tipo)) {
            gerarPDFTurmaAsync(jobId, referenciaId, solicitanteId);
        } else {
            gerarPDFAlunoAsync(jobId, referenciaId, null, solicitanteId);
        }

        return status;
    }

    public RelatorioStatusResponse solicitarRelatorioAluno(Long turmaId, UUID alunoId, UUID solicitanteId) {
        String jobId = UUID.randomUUID().toString();
        RelatorioStatusResponse status = new RelatorioStatusResponse(jobId, "PROCESSANDO", null);
        statusMap.put(jobId, status);
        gerarPDFAlunoAsync(jobId, turmaId, alunoId, solicitanteId);
        return status;
    }

    public RelatorioStatusResponse getStatus(String jobId) {
        return statusMap.getOrDefault(jobId,
                new RelatorioStatusResponse(jobId, "NAO_ENCONTRADO", null));
    }

    @Async
    public void gerarPDFTurmaAsync(String jobId, Long turmaId, UUID solicitanteId) {
        try {
            DesempenhoTurmaResponse desempenho = dashboardService.getDesempenhoTurma(turmaId, solicitanteId);
            String nomeArquivo = "turma_" + turmaId + "_" + timestamp() + ".pdf";
            Path caminho = criarArquivoPDF(nomeArquivo, doc -> {
                doc.add(new Paragraph("Relatório de Desempenho — " + desempenho.nomeTurma())
                        .setBold().setFontSize(16));
                doc.add(new Paragraph("Disciplina: " + desempenho.disciplina()));
                doc.add(new Paragraph("Média geral: " + desempenho.media()));
                doc.add(new Paragraph("% Aprovação: " + desempenho.percentualAprovacao() + "%"));
                doc.add(new Paragraph("% Frequência: " + desempenho.percentualFrequencia() + "%"));
                doc.add(new Paragraph("\nRanking de alunos:").setBold());
                desempenho.ranking().forEach(r ->
                        doc.add(new Paragraph(r.posicao() + "º " + r.nomeAluno()
                                + " — Média: " + r.media() + " (" + r.tendencia() + ")")));
            });

            String url = pdfBaseUrl + "/" + nomeArquivo;
            statusMap.put(jobId, new RelatorioStatusResponse(jobId, "CONCLUIDO", url));
            log.info("PDF de turma {} gerado: {}", turmaId, url);
        } catch (Exception e) {
            log.error("Erro ao gerar PDF turma {}: {}", turmaId, e.getMessage());
            statusMap.put(jobId, new RelatorioStatusResponse(jobId, "ERRO", null));
        }
    }

    @Async
    public void gerarPDFAlunoAsync(String jobId, Long turmaId, UUID alunoId, UUID solicitanteId) {
        try {
            DesempenhoAlunoResponse desempenho = dashboardService.getDesempenhoAluno(
                    turmaId, alunoId, solicitanteId, PerfilEnum.PROFESSOR);

            String nomeArquivo = "aluno_" + alunoId + "_turma_" + turmaId + "_" + timestamp() + ".pdf";
            criarArquivoPDF(nomeArquivo, doc -> {
                doc.add(new Paragraph("Relatório Individual — " + desempenho.nomeAluno())
                        .setBold().setFontSize(16));
                doc.add(new Paragraph("Situação: " + desempenho.situacao()));
                doc.add(new Paragraph("Média geral: " + desempenho.mediaGeral()));
                doc.add(new Paragraph("Frequência: " + desempenho.percentualFrequencia() + "%"));
                if (!desempenho.topicos().isEmpty()) {
                    doc.add(new Paragraph("\nDesempenho por tópico:").setBold());
                    desempenho.topicos().forEach(t ->
                            doc.add(new Paragraph(t.topico() + ": " + t.acertos() + "/" + t.total()
                                    + " (" + String.format("%.1f", t.percentual()) + "%)")));
                }
            });

            String url = pdfBaseUrl + "/" + nomeArquivo;
            statusMap.put(jobId, new RelatorioStatusResponse(jobId, "CONCLUIDO", url));
            log.info("PDF de aluno {} gerado: {}", alunoId, url);
        } catch (Exception e) {
            log.error("Erro ao gerar PDF aluno {}: {}", alunoId, e.getMessage());
            statusMap.put(jobId, new RelatorioStatusResponse(jobId, "ERRO", null));
        }
    }

    private Path criarArquivoPDF(String nomeArquivo, PdfConsumer consumer) throws IOException {
        Path dir = Paths.get(pdfDir);
        Files.createDirectories(dir);
        Path caminho = dir.resolve(nomeArquivo);

        try (PdfWriter writer = new PdfWriter(caminho.toFile());
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {
            consumer.accept(doc);
        }

        return caminho;
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    @FunctionalInterface
    interface PdfConsumer {
        void accept(Document doc);
    }
}
