package com.droneanalytics.backend.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class RegionMappingService {
    
    private Map<String, Long> centerCodeToRegionIdMap = new HashMap<>();
    
    @PostConstruct
    public void initRegionMapping() {
        // Полный маппинг center_code (русские) -> gid регионов
        centerCodeToRegionIdMap.put("Санкт-Петербургский", 14L); // City of St. Petersburg
        centerCodeToRegionIdMap.put("Московский", 43L);         // Moscow City
        centerCodeToRegionIdMap.put("Ростовский", 58L);         // Rostov
        centerCodeToRegionIdMap.put("Новосибирский", 50L);      // Novosibirsk
        centerCodeToRegionIdMap.put("Екатеринбургский", 66L);   // Sverdlovsk
        centerCodeToRegionIdMap.put("Самарский", 62L);          // Samara
        centerCodeToRegionIdMap.put("Калининградский", 21L);    // Kaliningrad
        centerCodeToRegionIdMap.put("Красноярский", 35L);       // Krasnoyarsk
        centerCodeToRegionIdMap.put("Иркутский", 18L);          // Irkutsk
        centerCodeToRegionIdMap.put("Хабаровский", 28L);        // Khabarovsk
        centerCodeToRegionIdMap.put("Владивостокский", 56L);    // Primor'ye
        
        // Дополнительные регионы если нужны
        centerCodeToRegionIdMap.put("Архангельский", 4L);       // Arkhangel'sk
        centerCodeToRegionIdMap.put("Астраханский", 5L);        // Astrakhan'
        centerCodeToRegionIdMap.put("Белгородский", 7L);        // Belgorod
        centerCodeToRegionIdMap.put("Волгоградский", 77L);      // Volgograd
        centerCodeToRegionIdMap.put("Воронежский", 79L);        // Voronezh
        centerCodeToRegionIdMap.put("Казанский", 68L);          // Tatarstan
        centerCodeToRegionIdMap.put("Нижегородский", 47L);      // Nizhegorod
        centerCodeToRegionIdMap.put("Омский", 51L);             // Omsk
        centerCodeToRegionIdMap.put("Пермский", 55L);           // Perm'
        centerCodeToRegionIdMap.put("Саратовский", 63L);        // Saratov
        centerCodeToRegionIdMap.put("Челябинский", 11L);        // Chelyabinsk
        centerCodeToRegionIdMap.put("Ярославский", 81L);        // Yaroslavl'
    }
    
    public Long getRegionIdByCenterCode(String centerCode) {
        return centerCodeToRegionIdMap.get(centerCode);
    }
}
