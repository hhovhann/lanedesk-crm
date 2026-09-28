package com.lanedesk.company;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CompanyImportController {

    private final CsvImportService importer;

    public CompanyImportController(CsvImportService importer) {
        this.importer = importer;
    }

    @GetMapping("/companies/import")
    String form() {
        return "import";
    }

    @PostMapping("/companies/import")
    String upload(@RequestParam MultipartFile file, RedirectAttributes flash) throws IOException {
        if (file.isEmpty()) {
            flash.addFlashAttribute("error", "Choose a CSV file first");
        } else {
            try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
                flash.addFlashAttribute("result", importer.importCsv(reader));
            }
        }
        return "redirect:/companies/import";
    }
}
