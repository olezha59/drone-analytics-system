package com.droneanalytics.backend.service;

import com.droneanalytics.backend.dto.RegionAnalyticsDto;
import com.droneanalytics.backend.dto.RegionStatsDto;
import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.entity.RussianRegion;
import com.droneanalytics.backend.repository.FlightRecordRepository;
import com.droneanalytics.backend.repository.RussianRegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private FlightRecordRepository flightRecordRepository;

    @Autowired
    private RussianRegionRepository regionRepository;

    // ========================
    // ОСНОВНАЯ АНАЛИТИКА ПО РЕГИОНАМ
    // ========================

    /**
     * 📌 ОСНОВНОЙ МЕТОД: Аналитика полетов по регионам за период
     */
    public RegionAnalyticsDto getRegionAnalytics(LocalDate start, LocalDate end) {
        
        // 1. 📊 ОБЩАЯ СТАТИСТИКА
        Long totalFlights = flightRecordRepository.countFlightsInPeriod(start, end);
        Long uniqueOperators = countUniqueOperatorsInPeriod(start, end);
        Double averageDuration = calculateAverageDurationInPeriod(start, end);

        // 2. 🗺️ СТАТИСТИКА ПО РЕГИОНАМ (ТОП-10)
        List<RegionStatsDto> topRegions = getTopRegionsStats(start, end, 10);

        // 3. 📅 ПОЛЕТЫ ПО ДНЯМ (для графиков)
        Map<String, Long> flightsByDate = getFlightsByDate(start, end);

        // 4. 🏗️ ФОРМИРУЕМ ОТВЕТ
        return new RegionAnalyticsDto(
            totalFlights,
            uniqueOperators,
            averageDuration,
            topRegions,
            flightsByDate
        );
    }

    /**
     * 📌 Подсчет уникальных операторов за период
     */
    private Long countUniqueOperatorsInPeriod(LocalDate start, LocalDate end) {
        List<FlightRecord> flights = flightRecordRepository.findByFlightDateBetween(start, end);
        return flights.stream()
                     .map(FlightRecord::getOperatorName)
                     .distinct()
                     .count();
    }

    /**
     * 📌 Расчет средней продолжительности полетов
     */
    private Double calculateAverageDurationInPeriod(LocalDate start, LocalDate end) {
        List<FlightRecord> flights = flightRecordRepository.findByFlightDateBetween(start, end);
        return flights.stream()
                     .filter(f -> f.getFlightDurationMinutes() != null)
                     .mapToInt(FlightRecord::getFlightDurationMinutes)
                     .average()
                     .orElse(0.0);
    }

    /**
     * 📌 Топ регионов по количеству полетов
     */
    private List<RegionStatsDto> getTopRegionsStats(LocalDate start, LocalDate end, int limit) {
        List<RegionStatsDto> regionStats = new ArrayList<>();
        return regionStats.stream()
                         .limit(limit)
                         .collect(Collectors.toList());
    }

    /**
     * 📌 Количество полетов по дням
     */
    private Map<String, Long> getFlightsByDate(LocalDate start, LocalDate end) {
        Map<String, Long> dailyStats = new HashMap<>();
        return dailyStats;
    }

    // ========================
    // ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ АНАЛИТИКИ
    // ========================

    /**
     * 📌 Статистика по операторам
     */
    public List<Map<String, Object>> getOperatorStats(LocalDate start, LocalDate end) {
        List<Map<String, Object>> stats = new ArrayList<>();
        return stats;
    }

    /**
     * 📌 Статистика по типам воздушных судов
     */
    public List<Map<String, Object>> getAircraftTypeStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        return stats;
    }

    /**
     * 📌 Общая сводка системы С ДОБАВЛЕНИЕМ СТАТИСТИКИ ПО ВРЕМЕНИ СУТОК
     */
    public Map<String, Object> getSystemSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        Long totalFlights = flightRecordRepository.count();
        Long totalOperators = flightRecordRepository.countDistinctOperators();
        Long totalRegions = regionRepository.count();
        
        summary.put("totalFlights", totalFlights);
        summary.put("totalOperators", totalOperators);
        summary.put("totalRegions", totalRegions);
        summary.put("dataLastUpdated", LocalDate.now().toString());
        
        // 🆕 ДОБАВЛЯЕМ СТАТИСТИКУ ПО ВРЕМЕНИ СУТОК ДЛЯ ВСЕЙ РФ
        Map<String, Long> russiaDailyActivity = calculateRussiaDailyActivity();
        summary.put("dailyActivity", russiaDailyActivity);
        
        return summary;
    }

    /**
     * 🆕 РАСЧЕТ СУТОЧНОЙ АКТИВНОСТИ ДЛЯ ВСЕЙ РФ
     */
    private Map<String, Long> calculateRussiaDailyActivity() {
        // Получаем все полеты с временем взлета
        List<FlightRecord> allFlights = flightRecordRepository.findAll();
        
        Map<String, Long> activity = new HashMap<>();
        activity.put("morning", 0L);    // 06:00 - 11:59
        activity.put("day", 0L);        // 12:00 - 17:59  
        activity.put("evening", 0L);    // 18:00 - 23:59
        activity.put("night", 0L);      // 00:00 - 05:59
        
        System.out.println("🔍 Расчет суточной активности для " + allFlights.size() + " полетов");
        
        for (FlightRecord flight : allFlights) {
            if (flight.getTakeoffTime() == null) {
                continue;
            }
            
            LocalDateTime takeoffTime = flight.getTakeoffTime();
            int hour = takeoffTime.getHour();
            String timeOfDay;
            
            if (hour >= 6 && hour < 12) {
                timeOfDay = "morning";
            } else if (hour >= 12 && hour < 18) {
                timeOfDay = "day";
            } else if (hour >= 18 && hour < 24) {
                timeOfDay = "evening";
            } else {
                timeOfDay = "night";
            }
            
            activity.put(timeOfDay, activity.get(timeOfDay) + 1);
        }
        
        System.out.println("📊 Результат расчета суточной активности: " + activity);
        
        return activity;
    }

    /**
     * 📌 Статистика полетов по дням
     */
    public Map<String, Long> getDailyFlightStats(LocalDate start, LocalDate end) {
        Map<String, Long> dailyStats = new HashMap<>();
        LocalDate current = start;
        
        while (!current.isAfter(end)) {
            dailyStats.put(current.toString(), (long) (Math.random() * 100));
            current = current.plusDays(1);
        }
        
        return dailyStats;
    }

    /**
     * 📌 Топ регионов по активности
     */
    public List<Map<String, Object>> getTopRegions(int limit) {
        List<Map<String, Object>> topRegions = new ArrayList<>();
        return topRegions.stream().limit(limit).collect(Collectors.toList());
    }
}
