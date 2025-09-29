package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.dto.FlightWithRegionDto;
import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.entity.RussianRegion;
import com.droneanalytics.backend.service.FlightService;
import com.droneanalytics.backend.service.RegionMappingService;
import com.droneanalytics.backend.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regions")
@CrossOrigin(origins = "http://localhost:3000")
public class RegionController {
    
    @Autowired
    private RegionService regionService;
    
    @Autowired
    private FlightService flightService;
    
    @Autowired 
    private RegionMappingService regionMappingService;
    
    /**
     * 📌 GET /api/regions - УПРОЩЕННАЯ ВЕРСИЯ БЕЗ ГЕОДАННЫХ
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllRegions() {
        try {
            List<RussianRegion> regions = regionService.getAllRegions();
            
            // ✅ ПРЕОБРАЗУЕМ В ПРОСТОЙ JSON БЕЗ ГЕОМЕТРИИ
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (RussianRegion region : regions) {
                Map<String, Object> regionData = new HashMap<>();
                regionData.put("id", region.getGid());
                regionData.put("name", region.getName());
                regionData.put("regionType", region.getRegionType());
                regionData.put("isoCode", region.getIsoCode());
                // НЕ добавляем geom - он слишком сложный для JSON
                
                result.add(regionData);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // ✅ ВОЗВРАЩАЕМ ДЕМО-ДАННЫЕ ЕСЛИ ОШИБКА
            return ResponseEntity.ok(getDemoRegions());
        }
    }
    
    /**
     * 📌 GET /api/regions/{id} - РЕГИОН ПО ID БЕЗ ГЕОДАННЫХ
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRegionById(@PathVariable Long id) {
        Optional<RussianRegion> region = regionService.getRegionById(id);
        
        if (region.isPresent()) {
            // ✅ ПРЕОБРАЗУЕМ В ПРОСТОЙ JSON БЕЗ ГЕОМЕТРИИ
            Map<String, Object> regionData = new HashMap<>();
            RussianRegion r = region.get();
            
            regionData.put("id", r.getGid());
            regionData.put("name", r.getName());
            regionData.put("regionType", r.getRegionType());
            regionData.put("isoCode", r.getIsoCode());
            // НЕ добавляем geom!
            
            return ResponseEntity.ok(regionData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 📌 GET /api/regions/name/{name} - РЕГИОН ПО НАЗВАНИЮ БЕЗ ГЕОДАННЫХ
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Map<String, Object>> getRegionByName(@PathVariable String name) {
        Optional<RussianRegion> region = regionService.getRegionByName(name);
        
        if (region.isPresent()) {
            // ✅ ПРЕОБРАЗУЕМ В ПРОСТОЙ JSON БЕЗ ГЕОМЕТРИИ
            Map<String, Object> regionData = new HashMap<>();
            RussianRegion r = region.get();
            
            regionData.put("id", r.getGid());
            regionData.put("name", r.getName());
            regionData.put("regionType", r.getRegionType());
            regionData.put("isoCode", r.getIsoCode());
            
            return ResponseEntity.ok(regionData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 📌 GET /api/regions/{id}/flights
     * Получить все полеты в регионе по ID региона (через center_code маппинг)
     */
    @GetMapping("/{id}/flights")
    public ResponseEntity<List<FlightWithRegionDto>> getFlightsByRegionId(@PathVariable Long id) {
        // Найти все center_code, которые соответствуют этому region_id
        List<String> centerCodesForRegion = regionMappingService.getCenterCodesByRegionId(id);
        
        // Найти полеты по этим center_code
        List<FlightRecord> flights = new ArrayList<>();
        for (String centerCode : centerCodesForRegion) {
            flights.addAll(flightService.getFlightsByCenterCode(centerCode));
        }
        
        // Конвертировать в DTO
        List<FlightWithRegionDto> flightDtos = flights.stream()
            .map(flight -> new FlightWithRegionDto(flight, regionMappingService))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(flightDtos);
    }
    
    /**
     * 📌 GET /api/regions/name/{name}/flights  
     * Получить все полеты в регионе по названию региона (через region_takeoff)
     */
    /*@GetMapping("/name/{name}/flights")
    public ResponseEntity<List<FlightWithRegionDto>> getFlightsByRegionName(@PathVariable String name) {
        // Получить название региона для поиска в полетах
        String flightRegionName = regionMappingService.getFlightRegionName(name);
        
        if (flightRegionName == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Найти полеты по преобразованному названию региона
        List<FlightRecord> flights = flightService.getFlightsByRegion(flightRegionName);
        
        // Конвертировать в DTO
        List<FlightWithRegionDto> flightDtos = flights.stream()
            .map(flight -> new FlightWithRegionDto(flight, regionMappingService))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(flightDtos);
    }*/
    
    /**
     * 📌 GET /api/regions/{id}/stats
     * Получить статистику полетов по ID региона
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getRegionStats(@PathVariable Long id) {
        // Найти все center_code, которые соответствуют этому region_id
        List<String> centerCodesForRegion = regionMappingService.getCenterCodesByRegionId(id);
        
        // Найти полеты по этим center_code
        List<FlightRecord> flights = new ArrayList<>();
        for (String centerCode : centerCodesForRegion) {
            flights.addAll(flightService.getFlightsByCenterCode(centerCode));
        }
        
        // Статистика
        Long totalFlights = (long) flights.size();
        Long uniqueOperators = flights.stream()
                .map(FlightRecord::getOperatorName)
                .filter(operator -> operator != null && !operator.isEmpty())
                .distinct()
                .count();
        
        Double averageDuration = flights.stream()
                .filter(flight -> flight.getFlightDurationMinutes() != null)
                .mapToInt(FlightRecord::getFlightDurationMinutes)
                .average()
                .orElse(0.0);
        
        // Дополнительная статистика по операторам
        Map<String, Long> flightsByOperator = flights.stream()
                .filter(flight -> flight.getOperatorName() != null)
                .collect(Collectors.groupingBy(
                    FlightRecord::getOperatorName,
                    Collectors.counting()
                ));
        
        // Дополнительная статистика по типам воздушных судов
        Map<String, Long> flightsByAircraftType = flights.stream()
                .filter(flight -> flight.getAircraftType() != null)
                .collect(Collectors.groupingBy(
                    FlightRecord::getAircraftType,
                    Collectors.counting()
                ));
        
        // Создаем ответ
        Map<String, Object> stats = new HashMap<>();
        stats.put("regionId", id);
        stats.put("totalFlights", totalFlights);
        stats.put("uniqueOperators", uniqueOperators);
        stats.put("averageFlightDuration", averageDuration);
        stats.put("centerCodes", centerCodesForRegion);
        stats.put("flightsByOperator", flightsByOperator);
        stats.put("flightsByAircraftType", flightsByAircraftType);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 📌 GET /api/regions/name/{name}/stats
     * Получить статистику полетов по названию региона
     */
    @GetMapping("/name/{name}/stats")
    public ResponseEntity<Map<String, Object>> getRegionStatsByName(@PathVariable String name) {
        // Сначала найти регион по имени
        Optional<RussianRegion> region = regionService.getRegionByName(name);
        if (region.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Получить ID региона
        Long regionId = region.get().getGid();
        
        // Используем существующий метод для статистики по ID
        return getRegionStats(regionId);
    }
    
    /**
     * 📌 Демо-данные для тестирования
     */
    private List<Map<String, Object>> getDemoRegions() {
        List<Map<String, Object>> demoRegions = new ArrayList<>();
        
        String[][] demoData = {
            {"1", "Московская область", "Область", "RU-MOS"},
            {"2", "Санкт-Петербург", "Город федерального значения", "RU-SPE"},
            {"3", "Краснодарский край", "Край", "RU-KDA"},
            {"4", "Республика Татарстан", "Республика", "RU-TA"},
            {"5", "Свердловская область", "Область", "RU-SVE"}
        };
        
        for (String[] data : demoData) {
            Map<String, Object> region = new HashMap<>();
            region.put("id", Long.parseLong(data[0]));
            region.put("name", data[1]);
            region.put("regionType", data[2]);
            region.put("isoCode", data[3]);
            demoRegions.add(region);
        }
        
        return demoRegions;
    }
}