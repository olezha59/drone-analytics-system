package com.droneanalytics.backend.service;

import com.droneanalytics.backend.dto.RegionAnalyticsDto;
import com.droneanalytics.backend.dto.RegionStatsDto;
import com.droneanalytics.backend.entity.FlightRecord;
import com.droneanalytics.backend.entity.RussianRegion;
import com.droneanalytics.backend.repository.FlightRecordRepository;
import com.droneanalytics.backend.repository.RussianRegionRepository;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
     * Этот метод будет вызываться когда пользователь выбирает период в веб-интерфейсе
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
        // Здесь будет сложный запрос с JOIN и группировкой
        // Пока используем упрощенную логику
        
        List<RegionStatsDto> regionStats = new ArrayList<>();
        
        // Пример данных (заглушка)
        regionStats.add(new RegionStatsDto("Московская область", 234L, 25L, 130.5));
        regionStats.add(new RegionStatsDto("Санкт-Петербург", 189L, 18L, 115.2));
        regionStats.add(new RegionStatsDto("Краснодарский край", 156L, 12L, 95.7));
        
        return regionStats.stream()
                         .limit(limit)
                         .collect(Collectors.toList());
    }

    /**
     * 📌 Количество полетов по дням
     */
    private Map<String, Long> getFlightsByDate(LocalDate start, LocalDate end) {
        Map<String, Long> dailyStats = new HashMap<>();
        
        // Пример данных (заглушка)
        dailyStats.put("2024-01-01", 45L);
        dailyStats.put("2024-01-02", 52L);
        dailyStats.put("2024-01-03", 38L);
        
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
        
        // Пример: оператор -> количество полетов
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("operatorName", "Аэроскан");
        stat1.put("flightCount", 156L);
        stat1.put("averageDuration", 125.5);
        
        Map<String, Object> stat2 = new HashMap<>();
        stat2.put("operatorName", "Геоскан");
        stat2.put("flightCount", 89L);
        stat2.put("averageDuration", 95.2);
        
        stats.add(stat1);
        stats.add(stat2);
        
        return stats;
    }

    /**
     * 📌 Статистика по типам воздушных судов
     */
    public List<Map<String, Object>> getAircraftTypeStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        // Пример данных
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("aircraftType", "DJI Phantom 4");
        stat1.put("flightCount", 245L);
        
        Map<String, Object> stat2 = new HashMap<>();
        stat2.put("aircraftType", "DJI Mavic 3");
        stat2.put("flightCount", 189L);
        
        stats.add(stat1);
        stats.add(stat2);
        
        return stats;
    }

    /**
     * 📌 Общая сводка системы
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
        
        return summary;
    }

    /**
     * 📌 Статистика полетов по дням
     */
    public Map<String, Long> getDailyFlightStats(LocalDate start, LocalDate end) {
        // Здесь будет сложный SQL запрос с группировкой по дате
        // Пока возвращаем пример данных
        
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
        
        // Пример данных
        Map<String, Object> region1 = new HashMap<>();
        region1.put("regionName", "Московская область");
        region1.put("flightCount", 234L);
        region1.put("operatorCount", 25L);
        
        Map<String, Object> region2 = new HashMap<>();
        region2.put("regionName", "Санкт-Петербург");
        region2.put("flightCount", 189L);
        region2.put("operatorCount", 18L);
        
        topRegions.add(region1);
        topRegions.add(region2);
        
        return topRegions.stream().limit(limit).collect(Collectors.toList());
    }
}
