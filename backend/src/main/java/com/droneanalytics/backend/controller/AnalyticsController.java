package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.dto.RegionAnalyticsDto;
import com.droneanalytics.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Аналитика", description = "API для аналитики и статистики полетов")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    // ========================
    // ОСНОВНЫЕ ENDPOINTS ДЛЯ АНАЛИТИКИ
    // ========================
    
    /**
     * 📌 GET /api/analytics/regions?dateFrom=2024-01-01&dateTo=2024-01-31
     * Аналитика полетов по регионам за период
     */
    @Operation(summary = "Аналитика по регионам", description = "Возвращает аналитику полетов по регионам за указанный период")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Аналитика успешно получена"),
        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/regions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<RegionAnalyticsDto> getRegionAnalytics(
            @Parameter(description = "Дата начала периода (ГГГГ-ММ-ДД)", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            
            @Parameter(description = "Дата окончания периода (ГГГГ-ММ-ДД)", example = "2024-12-31", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        try {
            RegionAnalyticsDto analytics = analyticsService.getRegionAnalytics(dateFrom, dateTo);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 📌 GET /api/analytics/operators?dateFrom=2024-01-01&dateTo=2024-01-31
     * Статистика по операторам
     */
    @Operation(summary = "Статистика по операторам", description = "Возвращает статистику полетов по операторам за период")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Статистика успешно получена"),
        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/operators")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<Map<String, Object>>> getOperatorAnalytics(
            @Parameter(description = "Дата начала периода (ГГГГ-ММ-ДД)", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            
            @Parameter(description = "Дата окончания периода (ГГГГ-ММ-ДД)", example = "2024-12-31", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        try {
            List<Map<String, Object>> operatorStats = analyticsService.getOperatorStats(dateFrom, dateTo);
            return ResponseEntity.ok(operatorStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 📌 GET /api/analytics/daily?dateFrom=2024-01-01&dateTo=2024-01-31
     * Количество полетов по дням
     */
    @Operation(summary = "Статистика по дням", description = "Возвращает количество полетов по дням за указанный период")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Статистика успешно получена"),
        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, Long>> getDailyFlights(
            @Parameter(description = "Дата начала периода (ГГГГ-ММ-ДД)", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            
            @Parameter(description = "Дата окончания периода (ГГГГ-ММ-ДД)", example = "2024-12-31", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        try {
            Map<String, Long> dailyStats = analyticsService.getDailyFlightStats(dateFrom, dateTo);
            return ResponseEntity.ok(dailyStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 📌 GET /api/analytics/top-regions?limit=10
     * Топ регионов по количеству полетов
     */
    @Operation(summary = "Топ регионов", description = "Возвращает список регионов с наибольшим количеством полетов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список успешно получен"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/top-regions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<Map<String, Object>>> getTopRegions(
            @Parameter(description = "Количество регионов в топе", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<Map<String, Object>> topRegions = analyticsService.getTopRegions(limit);
            return ResponseEntity.ok(topRegions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ========================
    // ENDPOINTS ДЛЯ ОБЩЕЙ СТАТИСТИКИ
    // ========================
    
    /**
     * 📌 GET /api/analytics/summary
     * Общая сводка по всем данным (С ДОБАВЛЕНИЕМ СУТОЧНОЙ АКТИВНОСТИ)
     */
    @Operation(summary = "Общая сводка", description = "Возвращает общую сводку по всем данным системы")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Сводка успешно получена"),
        @ApiResponse(responseCode = "400", description = "Ошибка при получении данных"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, Object>> getSummary() {
        try {
            Map<String, Object> summary = analyticsService.getSystemSummary();
            System.out.println("📊 Отправляемая статистика РФ: " + summary);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            System.err.println("❌ Ошибка в getSummary: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 📌 GET /api/analytics/aircraft-types
     * Статистика по типам воздушных судов
     */
    @Operation(summary = "Статистика по типам ВС", description = "Возвращает статистику по типам воздушных судов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Статистика успешно получена"),
        @ApiResponse(responseCode = "400", description = "Ошибка при получении данных"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    })
    @GetMapping("/aircraft-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<Map<String, Object>>> getAircraftTypeStats() {
        try {
            List<Map<String, Object>> stats = analyticsService.getAircraftTypeStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 📌 GET /api/analytics/regions-geojson
     * Получить GeoJSON с регионами
     */
    @Operation(summary = "GeoJSON регионов", description = "Возвращает GeoJSON файл с границами регионов")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GeoJSON успешно получен"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
        @ApiResponse(responseCode = "404", description = "GeoJSON файл не найден"),
        @ApiResponse(responseCode = "500", description = "Ошибка чтения файла")
    })
    @GetMapping("/regions-geojson")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<String> getRegionsGeoJSON() {
        try {
            Resource resource = new ClassPathResource("geo/regions.geojson");
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("GeoJSON file not found");
            }
            
            String geoJson = new String(Files.readAllBytes(Paths.get(resource.getURI())));
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(geoJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error reading GeoJSON file: " + e.getMessage());
        }
    }
}