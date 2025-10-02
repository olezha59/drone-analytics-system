package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.dto.RegionAnalyticsDto;
import com.droneanalytics.backend.service.AnalyticsService;
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
    @GetMapping("/regions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<RegionAnalyticsDto> getRegionAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
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
    @GetMapping("/operators")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<Map<String, Object>>> getOperatorAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
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
    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, Long>> getDailyFlights(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
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
    @GetMapping("/top-regions")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<Map<String, Object>>> getTopRegions(
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
