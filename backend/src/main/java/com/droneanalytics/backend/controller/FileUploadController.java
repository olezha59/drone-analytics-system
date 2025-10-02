package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("📥 Received file upload request");
            System.out.println("📄 File name: " + file.getOriginalFilename());
            System.out.println("📊 File size: " + file.getSize());
            System.out.println("🔍 Content type: " + file.getContentType());

            if (file.isEmpty()) {
                System.out.println("❌ File is empty");
                return ResponseEntity.badRequest().body("Файл пустой");
            }

            // Проверяем что это Excel файл
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            
            System.out.println("🔍 Checking file type...");
            
            boolean isExcelFile = (contentType != null && 
                    (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || 
                     contentType.equals("application/vnd.ms-excel"))) ||
                    (fileName != null && 
                     (fileName.toLowerCase().endsWith(".xlsx") || 
                      fileName.toLowerCase().endsWith(".xls")));

            if (!isExcelFile) {
                System.out.println("❌ Invalid file type: " + contentType);
                return ResponseEntity.badRequest().body("Поддерживаются только Excel файлы (.xlsx, .xls)");
            }

            System.out.println("✅ File validation passed, starting processing...");

            // Сохраняем файл и запускаем парсер
            String result = fileUploadService.processExcelFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Файл успешно обработан");
            response.put("details", result);
            response.put("filename", file.getOriginalFilename());
            response.put("size", String.valueOf(file.getSize()));

            System.out.println("✅ File processing completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error processing file: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Ошибка обработки файла");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
