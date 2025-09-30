// backend/src/main/java/com/droneanalytics/backend/controller/GeoController.java
package com.droneanalytics.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Paths;

@CrossOrigin(origins = "http://localhost:3000")  
@RestController
@RequestMapping("/api/geo")
public class GeoController {
    
    @GetMapping("/regions")
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
