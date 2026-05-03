package br.com.inovadados.teacherplatform.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentoParserService {

    private static final Set<String> STOPWORDS = Set.of(
            "a", "o", "e", "é", "de", "do", "da", "em", "um", "uma", "para",
            "com", "no", "na", "os", "as", "se", "que", "por", "mais", "mas",
            "ao", "dos", "das", "ou", "quando", "muito", "nos", "já", "eu",
            "também", "só", "pelo", "pela", "até", "isso", "ela", "entre",
            "era", "depois", "sem", "mesmo", "aos", "ter", "seu", "sua"
    );

    public String extrairTextoPDF(InputStream input) throws IOException {
        try (PDDocument doc = PDDocument.load(input)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc).trim();
        }
    }

    public String extrairTextoDocx(InputStream input) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(input)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append('\n');
                }
            }
            return sb.toString().trim();
        }
    }

    public int contarPalavrasUteis(String texto) {
        if (texto == null || texto.isBlank()) return 0;
        return (int) Arrays.stream(texto.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 2 && !STOPWORDS.contains(w))
                .count();
    }
}
