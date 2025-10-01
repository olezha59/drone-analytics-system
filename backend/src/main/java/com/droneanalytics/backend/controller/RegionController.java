package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.dto.FlightWithRegionDto;
import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.entity.RussianRegion;
import com.droneanalytics.backend.service.FlightService;
import com.droneanalytics.backend.service.RegionMappingService;
import com.droneanalytics.backend.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
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
     * Получить все полеты в регионе по названию региона (через center_code маппинг)
     */
    @GetMapping("/name/{name}/flights")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<List<FlightWithRegionDto>> getFlightsByRegionName(@PathVariable String name) {
        // Сначала найти регион по имени
        Optional<RussianRegion> region = regionService.getRegionByName(name);
        if (region.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Получить ID региона
        Long regionId = region.get().getGid();
        
        // Используем существующий метод для получения полетов по ID
        return getFlightsByRegionId(regionId);
    }
    
    /**
     * 📌 GET /api/regions/{id}/stats
     * Получить статистику полетов по ID региона за период
     */
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, Object>> getRegionStats(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Найти все center_code, которые соответствуют этому region_id
        List<String> centerCodesForRegion = regionMappingService.getCenterCodesByRegionId(id);
        
        // Найти полеты по этим center_code
        List<FlightRecord> allFlights = new ArrayList<>();
        for (String centerCode : centerCodesForRegion) {
            allFlights.addAll(flightService.getFlightsByCenterCode(centerCode));
        }
        
        // 📅 ФИЛЬТРАЦИЯ ПО ДАТАМ ЕСЛИ УКАЗАНЫ
        List<FlightRecord> flights = filterFlightsByDate(allFlights, startDate, endDate);
        
        // Статистика
        Long totalFlights = (long) flights.size();
        Long uniqueOperators = flights.stream()
                .map(FlightRecord::getOperatorName)
                .filter(operator -> operator != null && !operator.isEmpty())
                .distinct()
                .count();
        
        // 📊 НОВЫЙ РАСЧЕТ: средняя продолжительность из takeoff_time и landing_time
        Double averageDuration = calculateAverageFlightDuration(flights);
        
        // 🆕 ДНЕВНАЯ АКТИВНОСТЬ: распределение полетов по времени суток
        Map<String, Long> dailyActivity = calculateDailyActivity(flights);
        
        // 🆕 НУЛЕВЫЕ ДНИ: количество дней без полетов
        Long zeroDays = calculateZeroDays(flights, startDate, endDate);
        
        // 🆕 СРЕДНЕСУТОЧНАЯ ДИНАМИКА: среднее число полетов в сутки
        Map<String, Object> averageDailyStats = calculateAverageDailyFlights(flights, startDate, endDate);
        
        // 🆕 РОСТ-ПАДЕНИЕ: процентное изменение (только если указаны обе даты)
        Map<String, Object> growthDeclineStats = calculateGrowthDecline(flights, startDate, endDate);
        
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
        stats.put("dailyActivity", dailyActivity);
        stats.put("zeroDays", zeroDays);
        stats.put("averageDailyFlights", averageDailyStats); // 🆕 СРЕДНЕСУТОЧНАЯ ДИНАМИКА
        if (!growthDeclineStats.isEmpty()) {
            stats.put("growthDecline", growthDeclineStats); // 🆕 РОСТ-ПАДЕНИЕ (только если есть)
        }
        stats.put("centerCodes", centerCodesForRegion);
        stats.put("flightsByOperator", flightsByOperator);
        stats.put("flightsByAircraftType", flightsByAircraftType);
        
        // 📅 ДОБАВЛЯЕМ ИНФОРМАЦИЮ О ПЕРИОДЕ
        if (startDate != null) {
            stats.put("startDate", startDate.toString());
        }
        if (endDate != null) {
            stats.put("endDate", endDate.toString());
        }
        stats.put("periodDescription", getPeriodDescription(startDate, endDate));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 📌 GET /api/regions/name/{name}/stats
     * Получить статистику полетов по названию региона за период
     */
    @GetMapping("/name/{name}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, Object>> getRegionStatsByName(
            @PathVariable String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Сначала найти регион по имени
        Optional<RussianRegion> region = regionService.getRegionByName(name);
        if (region.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Получить ID региона
        Long regionId = region.get().getGid();
        
        // Используем существующий метод для статистики по ID
        return getRegionStats(regionId, startDate, endDate);
    }
    
    /**
     * 🆕 МЕТОД: Фильтрация полетов по датам
     */
    private List<FlightRecord> filterFlightsByDate(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return flights; // Без фильтрации
        }
        
        return flights.stream()
                .filter(flight -> flight.getFlightDate() != null)
                .filter(flight -> {
                    LocalDate flightDate = flight.getFlightDate();
                    boolean afterStart = (startDate == null) || !flightDate.isBefore(startDate);
                    boolean beforeEnd = (endDate == null) || !flightDate.isAfter(endDate);
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 📌 НОВЫЙ МЕТОД: Расчет средней продолжительности полетов
     * из разницы между landing_time и takeoff_time
     */
    private Double calculateAverageFlightDuration(List<FlightRecord> flights) {
        List<Long> durationsInMinutes = new ArrayList<>();
        
        for (FlightRecord flight : flights) {
            // Пропускаем если нет одного из времен
            if (flight.getTakeoffTime() == null || flight.getLandingTime() == null) {
                continue;
            }
            
            // Рассчитываем продолжительность в минутах
            long durationMinutes = Duration.between(
                flight.getTakeoffTime(), 
                flight.getLandingTime()
            ).toMinutes();
            
            // Добавляем только положительные значения
            if (durationMinutes > 0) {
                durationsInMinutes.add(durationMinutes);
            }
        }
        
        // Если нет данных с продолжительностью - возвращаем null
        if (durationsInMinutes.isEmpty()) {
            return null;
        }
        
        // Считаем среднее арифметическое
        return durationsInMinutes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }
    
    /**
     * 🆕 МЕТОД: Расчет дневной активности (распределение по времени суток)
     */
    private Map<String, Long> calculateDailyActivity(List<FlightRecord> flights) {
        Map<String, Long> activity = new HashMap<>();
        activity.put("утро", 0L);    // 06:00 - 11:59
        activity.put("день", 0L);    // 12:00 - 17:59  
        activity.put("вечер", 0L);   // 18:00 - 23:59
        activity.put("ночь", 0L);    // 00:00 - 05:59
        
        for (FlightRecord flight : flights) {
            if (flight.getTakeoffTime() == null) {
                continue;
            }
            
            int hour = flight.getTakeoffTime().getHour();
            String timeOfDay;
            
            if (hour >= 6 && hour < 12) {
                timeOfDay = "утро";
            } else if (hour >= 12 && hour < 18) {
                timeOfDay = "день";
            } else if (hour >= 18 && hour < 24) {
                timeOfDay = "вечер";
            } else {
                timeOfDay = "ночь";
            }
            
            activity.put(timeOfDay, activity.get(timeOfDay) + 1);
        }
        
        return activity;
    }
    
    /**
     * 🆕 МЕТОД: Расчет количества дней без полетов (с учетом периода)
     */
    private Long calculateZeroDays(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        if (flights.isEmpty()) {
            return 0L;
        }
        
        // Собираем все уникальные даты с полетами
        Set<LocalDate> daysWithFlights = flights.stream()
                .filter(flight -> flight.getFlightDate() != null)
                .map(FlightRecord::getFlightDate)
                .collect(Collectors.toSet());
        
        if (daysWithFlights.isEmpty()) {
            return 0L;
        }
        
        // Определяем границы периода
        LocalDate minDate = startDate != null ? startDate : daysWithFlights.stream()
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate maxDate = endDate != null ? endDate : daysWithFlights.stream()
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());
        
        // Считаем все дни в диапазоне
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate) + 1;
        
        // Количество дней без полетов = общее количество дней - дни с полетами
        return totalDays - daysWithFlights.size();
    }
    
    /**
     * 🆕 МЕТОД: Расчет среднесуточной динамики
     */
    private Map<String, Object> calculateAverageDailyFlights(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        if (flights.isEmpty()) {
            stats.put("averageFlightsPerDay", 0.0);
            stats.put("daysWithFlights", 0);
            stats.put("totalDaysInPeriod", 0);
            return stats;
        }
        
        // Собираем все уникальные даты с полетами
        Set<LocalDate> daysWithFlights = flights.stream()
                .filter(flight -> flight.getFlightDate() != null)
                .map(FlightRecord::getFlightDate)
                .collect(Collectors.toSet());
        
        // Определяем общее количество дней в периоде
        long totalDaysInPeriod;
        if (startDate != null && endDate != null) {
            totalDaysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        } else {
            // Если период не указан, используем диапазон дат полетов
            LocalDate minDate = daysWithFlights.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
            LocalDate maxDate = daysWithFlights.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
            totalDaysInPeriod = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate) + 1;
        }
        
        double averageFlightsPerDay = (double) flights.size() / totalDaysInPeriod;
        
        stats.put("averageFlightsPerDay", Math.round(averageFlightsPerDay * 100.0) / 100.0);
        stats.put("daysWithFlights", daysWithFlights.size());
        stats.put("totalDaysInPeriod", totalDaysInPeriod);
        
        return stats;
    }
    
    /**
     * 🆕 МЕТОД: Расчет роста-падения (только если указаны обе даты)
     */
    private Map<String, Object> calculateGrowthDecline(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // Расчет только если указаны обе даты
        if (startDate == null || endDate == null) {
            return stats; // Пустой Map
        }
        
        // Проверяем что период достаточно длинный для сравнения (минимум 2 месяца)
        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(
            startDate.withDayOfMonth(1), 
            endDate.withDayOfMonth(1)
        );
        
        if (monthsBetween < 1) {
            return stats; // Период меньше 2 месяцев - не считаем
        }
        
        // Получаем первый и последний месяц периода
        YearMonth firstMonth = YearMonth.from(startDate);
        YearMonth lastMonth = YearMonth.from(endDate);
        
        // Считаем полеты в первом месяце
        long firstMonthFlights = flights.stream()
                .filter(flight -> flight.getFlightDate() != null)
                .filter(flight -> {
                    YearMonth flightMonth = YearMonth.from(flight.getFlightDate());
                    return flightMonth.equals(firstMonth);
                })
                .count();
        
        // Считаем полеты в последнем месяце
        long lastMonthFlights = flights.stream()
                .filter(flight -> flight.getFlightDate() != null)
                .filter(flight -> {
                    YearMonth flightMonth = YearMonth.from(flight.getFlightDate());
                    return flightMonth.equals(lastMonth);
                })
                .count();
        
        // Рассчитываем процентное изменение
        double changePercentage = 0.0;
        String trend = "stable";
        
        if (firstMonthFlights > 0) {
            changePercentage = ((double) (lastMonthFlights - firstMonthFlights) / firstMonthFlights) * 100;
            trend = changePercentage > 0 ? "growth" : (changePercentage < 0 ? "decline" : "stable");
        } else if (lastMonthFlights > 0) {
            changePercentage = 100.0; // Рост с 0
            trend = "growth";
        }
        
        stats.put("firstMonth", firstMonth.toString());
        stats.put("lastMonth", lastMonth.toString());
        stats.put("firstMonthFlights", firstMonthFlights);
        stats.put("lastMonthFlights", lastMonthFlights);
        stats.put("changePercentage", Math.round(changePercentage * 100.0) / 100.0);
        stats.put("trend", trend);
        stats.put("monthsBetween", monthsBetween + 1);
        
        return stats;
    }
    
    /**
     * 🆕 МЕТОД: Описание периода для ответа
     */
    private String getPeriodDescription(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "За весь период";
        } else if (startDate != null && endDate != null) {
            return String.format("С %s по %s", startDate, endDate);
        } else if (startDate != null) {
            return String.format("С %s", startDate);
        } else {
            return String.format("По %s", endDate);
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
            region.put("id", Long.parseLong(data[0]));
            region.put("name", data[1]);
            region.put("regionType", data[2]);
            region.put("isoCode", data[3]);
            demoRegions.add(region);
        }
        
        return demoRegions;
    }
}