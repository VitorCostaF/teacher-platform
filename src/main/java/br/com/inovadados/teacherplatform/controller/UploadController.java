package br.com.inovadados.teacherplatform.controller;

import br.com.inovadados.teacherplatform.service.DocumentoParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN', 'COORDENADOR')")
public class UploadController {

    private static final int MINIMO_PALAVRAS_UTEIS = 100;

    private final DocumentoParserService documentoParserService;

    @PostMapping("/conteudo")
    public ResponseEntity<Map<String, Object>> uploadConteudo(
            @RequestParam("file") MultipartFile file) throws IOException {

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean isPdf = (contentType != null && contentType.contains("pdf")) || filename.endsWith(".pdf");
        boolean isDocx = (contentType != null && contentType.contains("wordprocessingml"))
                || filename.endsWith(".docx");

        if (!isPdf && !isDocx) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "FORMATO_NAO_SUPORTADO",
                    "message", "Apenas arquivos PDF e DOCX são aceitos"
            ));
        }

        String texto = isPdf
                ? documentoParserService.extrairTextoPDF(file.getInputStream())
                : documentoParserService.extrairTextoDocx(file.getInputStream());

        int palavrasUteis = documentoParserService.contarPalavrasUteis(texto);
        boolean conteudoInsuficiente = palavrasUteis < MINIMO_PALAVRAS_UTEIS;

        return ResponseEntity.ok(Map.of(
                "texto", texto,
                "palavrasUteis", palavrasUteis,
                "avisoConteudoInsuficiente", conteudoInsuficiente
        ));
    }
}
