package com.droneanalytics.backend.controller;

import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class HealthCheckController {

    @GetMapping("/health/python")
    public String checkPython() {
        try {
            // Проверяем доступность Python
            Process process = new ProcessBuilder("python3", "--version").start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                return "✅ Python 3 доступен";
            } else {
                return "❌ Python 3 недоступен";
            }
        } catch (Exception e) {
            return "❌ Ошибка проверки Python: " + e.getMessage();
        }
    }

    @GetMapping("/health/script")
    public String checkScript() {
        String[] possiblePaths = {
            "parser_vscode.py",
            "../parser_vscode.py", 
            "../../parser_vscode.py",
            "backend/parser_vscode.py",
            "../backend/parser_vscode.py"
        };
        
        StringBuilder result = new StringBuilder("Поиск Python скрипта:\\n");
        
        for (String path : possiblePaths) {
            File candidate = new File(path);
            result.append("🔍 ").append(path).append(": ")
                  .append(candidate.exists() ? "✅ НАЙДЕН" : "❌ НЕ НАЙДЕН")
                  .append(" (").append(candidate.getAbsolutePath()).append(")\\n");
        }
        
        return result.toString();
    }
}
