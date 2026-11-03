package com.collectsmart.controller;

import com.collectsmart.service.CsvImportService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/csv")
public class CsvImportController {

private final CsvImportService csvImportService;

public CsvImportController(CsvImportService csvImportService) {
    this.csvImportService = csvImportService;
}

@PostMapping(
    value = "/upload",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
)
public ResponseEntity<?> uploadCsv(
        @RequestParam("file") MultipartFile file) {

    try {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Please select a CSV file."));
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only CSV files are allowed."));
        }

        Map<String, Object> result =
                csvImportService.importLoans(file);

        return ResponseEntity.ok(result);

    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}

}