package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.entity.RussianRegion;
import com.droneanalytics.backend.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/regions")
@CrossOrigin(origins = "http://localhost:3000")
public class RegionController {
    
    @Autowired
    private RegionService regionService;
    
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
            region.put("gid", Long.parseLong(data[0]));
            region.put("name", data[1]);
            region.put("regionType", data[2]);
            region.put("isoCode", data[3]);
            demoRegions.add(region);
        }
        
        return demoRegions;
    }
    
    // ... остальные методы без изменений
    @GetMapping("/{id}")
    public ResponseEntity<RussianRegion> getRegionById(@PathVariable Long id) {
        Optional<RussianRegion> region = regionService.getRegionById(id);
        return region.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<RussianRegion> getRegionByName(@PathVariable String name) {
        Optional<RussianRegion> region = regionService.getRegionByName(name);
        return region.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
}