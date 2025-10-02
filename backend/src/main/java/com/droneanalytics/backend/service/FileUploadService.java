package com.droneanalytics.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {

    private final String UPLOAD_DIR = "uploads";

    public String processExcelFile(MultipartFile file) throws IOException, InterruptedException {
        System.out.println("📁 Starting file processing...");
        
        // Создаем директорию для загрузок если её нет
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("✅ Created upload directory: " + uploadPath.toAbsolutePath());
        }

        // Сохраняем файл
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✅ File saved: " + filePath.toAbsolutePath());
        System.out.println("🐍 Starting Python parser...");

        // Запускаем Python парсер
        return runPythonParser(filePath.toAbsolutePath().toString());
    }

    private String runPythonParser(String excelFilePath) throws IOException, InterruptedException {
        try {
            // Ищем Python скрипт в нескольких местах
            String[] possiblePaths = {
                "parser_vscode.py",                    // Текущая директория
                "../parser_vscode.py",                 // Родительская директория  
                "../../parser_vscode.py",              // На два уровня выше
                "backend/parser_vscode.py",            // В папке backend
                "../backend/parser_vscode.py"          // В папке backend на уровень выше
            };
            
            File pythonScript = null;
            for (String path : possiblePaths) {
                File candidate = new File(path);
                System.out.println("🔍 Checking: " + candidate.getAbsolutePath());
                if (candidate.exists()) {
                    pythonScript = candidate;
                    System.out.println("✅ Found Python script at: " + pythonScript.getAbsolutePath());
                    break;
                }
            }
            
            if (pythonScript == null) {
                throw new IOException("Python скрипт не найден. Проверенные пути: " + String.join(", ", possiblePaths));
            }

            System.out.println("🚀 Executing: python3 " + pythonScript.getAbsolutePath() + " --file " + excelFilePath);

            // Создаем процесс для запуска Python скрипта
            ProcessBuilder processBuilder = new ProcessBuilder(
                "python3", 
                pythonScript.getAbsolutePath(),
                "--file",
                excelFilePath
            );

            // Устанавливаем рабочую директорию
            processBuilder.directory(new File("."));
            
            // Направляем вывод в консоль
            processBuilder.redirectErrorStream(true);
            processBuilder.inheritIO();

            // Запускаем процесс
            Process process = processBuilder.start();
            System.out.println("⏳ Waiting for Python script to complete...");
            
            // Ждем завершения с таймаутом (10 минут)
            boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.MINUTES);
            
            if (!finished) {
                process.destroy();
                throw new IOException("Python скрипт превысил время выполнения (10 минут)");
            }
            
            int exitCode = process.exitValue();
            System.out.println("🐍 Python script finished with exit code: " + exitCode);
            
            if (exitCode == 0) {
                return "Данные успешно обработаны и загружены в базу данных";
            } else {
                throw new IOException("Python скрипт завершился с ошибкой. Код: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("❌ Python script execution failed: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Ошибка запуска Python парсера: " + e.getMessage(), e);
        }
    }
}
