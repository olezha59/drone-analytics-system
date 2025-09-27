package com.droneanalytics.backend.service;

import com.droneanalytics.backend.entity.RussianRegion;
import com.droneanalytics.backend.repository.RussianRegionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegionService {

    @Autowired
    private RussianRegionRepository regionRepository;

    /**
     * 📌 Получить все регионы
     */
    public List<RussianRegion> getAllRegions() {
        return regionRepository.findAll();
    }

    /**
     * 📌 Найти регион по ID
     */
    public Optional<RussianRegion> getRegionById(Long id) {
        return regionRepository.findById(id);
    }

    /**
     * 📌 Найти регион по названию
     */
    public Optional<RussianRegion> getRegionByName(String name) {
        return regionRepository.findByName(name);
    }

    /**
     * 📌 Поиск регионов по части названия
     */
    public List<RussianRegion> searchRegionsByName(String namePart) {
        return regionRepository.findByNameContainingIgnoreCase(namePart);
    }

    /**
     * 📌 Получить регионы по типу
     */
    public List<RussianRegion> getRegionsByType(String regionType) {
        return regionRepository.findByRegionType(regionType);
    }

    /**
     * 📌 Найти регион по координатам (геопоиск)
     */
    public Optional<RussianRegion> findRegionByCoordinates(Double latitude, Double longitude) {
        // Здесь будет сложная логика с преобразованием координат в Point
        // и вызовом геопространственного запроса
        // Пока возвращаем первый регион как пример
        return regionRepository.findAll().stream().findFirst();
    }
}
