package br.com.inovadados.teacherplatform.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanilhaParserService {

    public List<Map<String, String>> parsearCSV(InputStream input) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            for (CSVRecord record : parser) {
                result.add(new LinkedHashMap<>(record.toMap()));
            }
        }
        return result;
    }

    public List<Map<String, String>> parsearXLSX(InputStream input) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return result;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.toString().trim());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowMap.put(headers.get(j), cell != null ? cell.toString().trim() : "");
                }
                result.add(rowMap);
            }
        }
        return result;
    }

    public List<Map<String, String>> parsear(InputStream input, String contentType, String filename) throws IOException {
        boolean isXlsx = (contentType != null && contentType.contains("spreadsheetml"))
                || (filename != null && filename.toLowerCase().endsWith(".xlsx"));
        return isXlsx ? parsearXLSX(input) : parsearCSV(input);
    }
}
