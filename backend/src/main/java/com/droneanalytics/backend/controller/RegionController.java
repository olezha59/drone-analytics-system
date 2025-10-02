package com.droneanalytics.backend.controller;

import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.service.FlightService;
import com.droneanalytics.backend.service.RegionMappingService;
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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@RestController
@RequestMapping("/api/regions")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Регионы", description = "API для работы с регионами и статистикой полетов")
@SecurityRequirement(name = "bearerAuth")
public class RegionController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private RegionMappingService regionMappingService;

    // ... другие методы ...

    /**
     * 📌 GET /api/regions/{id}/stats
     * Получить статистику полетов по ID региона за период
     */
    @Operation(summary = "Получить статистику по региону", description = "Возвращает детальную статистику полетов для указанного региона за период")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Статистика успешно получена"),
        @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
        @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
        @ApiResponse(responseCode = "404", description = "Регион не найден")
    })
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Map<String, Object>> getRegionStats(
            @Parameter(description = "ID региона", example = "1") 
            @PathVariable Long id,
            
            @Parameter(description = "Дата начала периода (ГГГГ-ММ-ДД)", example = "2024-01-01") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Дата окончания периода (ГГГГ-ММ-ДД)", example = "2024-12-31") 
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
        
        // 🆕 РАСПРЕДЕЛЕНИЕ ПО ГОДАМ: новый метод
        Map<String, Object> yearlyStats = calculateYearlyDistribution(flights);
        Map<Integer, Long> yearlyDistribution = (Map<Integer, Long>) yearlyStats.get("yearlyDistribution");
        Map<String, Object> mostActiveYear = (Map<String, Object>) yearlyStats.get("mostActiveYear");
        
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
        stats.put("averageDailyFlights", averageDailyStats);
        stats.put("yearlyDistribution", yearlyDistribution); // 🆕 ДОБАВЛЯЕМ РАСПРЕДЕЛЕНИЕ ПО ГОДАМ
        stats.put("mostActiveYear", mostActiveYear); // 🆕 ДОБАВЛЯЕМ САМЫЙ АКТИВНЫЙ ГОД
        if (!growthDeclineStats.isEmpty()) {
            stats.put("growthDecline", growthDeclineStats);
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
     * 🆕 МЕТОД: Расчет распределения полетов по годам с определением самого активного года
     */
    private Map<String, Object> calculateYearlyDistribution(List<FlightRecord> flights) {
        Map<Integer, Long> yearlyStats = new HashMap<>();
        
        // Собираем статистику по годам
        for (FlightRecord flight : flights) {
            if (flight.getFlightDate() != null) {
                int year = flight.getFlightDate().getYear();
                yearlyStats.put(year, yearlyStats.getOrDefault(year, 0L) + 1);
            }
        }
        
        // Находим самый активный год
        Map.Entry<Integer, Long> mostActive = yearlyStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        Map<String, Object> result = new HashMap<>();
        result.put("yearlyDistribution", yearlyStats);
        
        if (mostActive != null) {
            Map<String, Object> mostActiveYear = new HashMap<>();
            mostActiveYear.put("year", mostActive.getKey());
            mostActiveYear.put("flightsCount", mostActive.getValue());
            result.put("mostActiveYear", mostActiveYear);
        }
        
        return result;
    }

    /**
     * 📌 Расчет количества дней без полетов
     */
    private Long calculateZeroDays(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0L;
        }
        
        Set<LocalDate> daysWithFlights = flights.stream()
                .map(FlightRecord::getFlightDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        long totalDays = startDate.until(endDate).getDays() + 1;
        long daysWithFlightsCount = daysWithFlights.size();
        
        return totalDays - daysWithFlightsCount;
    }

    /**
     * 📌 Расчет среднесуточной статистики
     */
    private Map<String, Object> calculateAverageDailyFlights(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        if (startDate == null || endDate == null) {
            stats.put("daysWithFlights", 0);
            stats.put("averageFlightsPerDay", 0.0);
            stats.put("totalDaysInPeriod", 0);
            return stats;
        }
        
        Set<LocalDate> daysWithFlights = flights.stream()
                .map(FlightRecord::getFlightDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        long totalDays = startDate.until(endDate).getDays() + 1;
        long daysWithFlightsCount = daysWithFlights.size();
        double averageFlightsPerDay = daysWithFlightsCount > 0 ? (double) flights.size() / daysWithFlightsCount : 0.0;
        
        stats.put("daysWithFlights", (int) daysWithFlightsCount);
        stats.put("averageFlightsPerDay", Math.round(averageFlightsPerDay * 100.0) / 100.0);
        stats.put("totalDaysInPeriod", (int) totalDays);
        
        return stats;
    }

    /**
     * 📌 Расчет роста/падения активности
     */
    private Map<String, Object> calculateGrowthDecline(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        if (startDate == null || endDate == null || flights.isEmpty()) {
            return stats;
        }
        
        // Группируем полеты по месяцам
        Map<String, Long> flightsByMonth = flights.stream()
                .filter(flight -> flight.getFlightDate() != null)
                .collect(Collectors.groupingBy(
                    flight -> flight.getFlightDate().getYear() + "-" + 
                             String.format("%02d", flight.getFlightDate().getMonthValue()),
                    Collectors.counting()
                ));
        
        // Находим первый и последний месяц
        List<String> months = new ArrayList<>(flightsByMonth.keySet());
        if (months.size() < 2) {
            return stats;
        }
        
        Collections.sort(months);
        String firstMonth = months.get(0);
        String lastMonth = months.get(months.size() - 1);
        
        Long firstMonthFlights = flightsByMonth.get(firstMonth);
        Long lastMonthFlights = flightsByMonth.get(lastMonth);
        
        double changePercentage = 0.0;
        String trend = "stable";
        
        if (firstMonthFlights != null && lastMonthFlights != null && firstMonthFlights > 0) {
            changePercentage = ((lastMonthFlights - firstMonthFlights) / (double) firstMonthFlights) * 100;
            trend = changePercentage > 0 ? "growth" : (changePercentage < 0 ? "decline" : "stable");
        }
        
        stats.put("firstMonthFlights", firstMonthFlights != null ? firstMonthFlights : 0);
        stats.put("lastMonthFlights", lastMonthFlights != null ? lastMonthFlights : 0);
        stats.put("trend", trend);
        stats.put("firstMonth", firstMonth);
        stats.put("lastMonth", lastMonth);
        stats.put("monthsBetween", months.size());
        stats.put("changePercentage", Math.round(changePercentage * 100.0) / 100.0);
        
        return stats;
    }
    
    private List<FlightRecord> filterFlightsByDate(List<FlightRecord> flights, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return flights;
        }
        
        return flights.stream()
                .filter(flight -> {
                    if (flight.getFlightDate() == null) return false;
                    if (startDate != null && flight.getFlightDate().isBefore(startDate)) return false;
                    if (endDate != null && flight.getFlightDate().isAfter(endDate)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    private Double calculateAverageFlightDuration(List<FlightRecord> flights) {
        return flights.stream()
                .filter(f -> f.getFlightDurationMinutes() != null)
                .mapToInt(FlightRecord::getFlightDurationMinutes)
                .average()
                .orElse(0.0);
    }
    
    private Map<String, Long> calculateDailyActivity(List<FlightRecord> flights) {
        Map<String, Long> activity = new HashMap<>();
        activity.put("morning", 0L);
        activity.put("day", 0L);
        activity.put("evening", 0L);
        activity.put("night", 0L);
        
        for (FlightRecord flight : flights) {
            if (flight.getTakeoffTime() != null) {
                int hour = flight.getTakeoffTime().getHour();
                if (hour >= 6 && hour < 12) {
                    activity.put("morning", activity.get("morning") + 1);
                } else if (hour >= 12 && hour < 18) {
                    activity.put("day", activity.get("day") + 1);
                } else if (hour >= 18 && hour < 24) {
                    activity.put("evening", activity.get("evening") + 1);
                } else {
                    activity.put("night", activity.get("night") + 1);
                }
            }
        }
        
        return activity;
    }
    
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
}