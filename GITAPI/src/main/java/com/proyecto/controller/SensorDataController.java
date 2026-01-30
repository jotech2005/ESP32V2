package com.proyecto.controller;

import com.proyecto.model.SensorData;
import com.proyecto.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sensor-data")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SensorDataController {

    @Autowired
    private SensorDataService sensorDataService;

    // =====================================================
    // CREATE - POST: Recibir datos del ESP32
    // =====================================================
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSensorData(@RequestBody SensorData sensorData) {
        try {
            SensorData savedData = sensorDataService.saveSensorData(sensorData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Datos guardados correctamente");
            response.put("id", savedData.getId());
            response.put("timestamp", savedData.getTimestamp());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al guardar datos: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // =====================================================
    // READ - GET: Obtener todos los datos
    // =====================================================
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSensorData() {
        try {
            List<SensorData> data = sensorDataService.getAllSensorData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", data.size());
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener datos: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =====================================================
    // READ - GET: Obtener por ID
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSensorDataById(@PathVariable Long id) {
        try {
            Optional<SensorData> data = sensorDataService.getSensorDataById(id);
            
            Map<String, Object> response = new HashMap<>();
            
            if (data.isPresent()) {
                response.put("success", true);
                response.put("data", data.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Registro no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener datos: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =====================================================
    // READ - GET: Últimas N lecturas
    // =====================================================
    @GetMapping("/latest/{limit}")
    public ResponseEntity<Map<String, Object>> getLatestReadings(@PathVariable int limit) {
        try {
            List<SensorData> data = sensorDataService.getLastNReadings(Math.min(limit, 100));
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("limit", limit);
            response.put("total", data.size());
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener datos: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =====================================================
    // READ - GET: Buscar por RFID
    // =====================================================
    @GetMapping("/rfid/{rfidTag}")
    public ResponseEntity<Map<String, Object>> getDataByRfidTag(@PathVariable String rfidTag) {
        try {
            List<SensorData> data = sensorDataService.getDataByRfidTag(rfidTag);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("rfid_tag", rfidTag);
            response.put("total", data.size());
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener datos: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =====================================================
    // READ - GET: Buscar por rango de fechas
    // =====================================================
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getDataByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);
            
            List<SensorData> data = sensorDataService.getDataByDateRange(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("start_date", startDate);
            response.put("end_date", endDate);
            response.put("total", data.size());
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // =====================================================
    // READ - GET: Datos con luz detectada
    // =====================================================
    @GetMapping("/light-detected")
    public ResponseEntity<Map<String, Object>> getDataWithLightDetected() {
        try {
            List<SensorData> data = sensorDataService.getDataWithLightDetected();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", data.size());
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al obtener datos: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =====================================================
    // UPDATE - PUT: Actualizar registro
    // =====================================================
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSensorData(
            @PathVariable Long id,
            @RequestBody SensorData sensorData) {
        try {
            SensorData updatedData = sensorDataService.updateSensorData(id, sensorData);
            
            Map<String, Object> response = new HashMap<>();
            
            if (updatedData != null) {
                response.put("success", true);
                response.put("message", "Registro actualizado");
                response.put("data", updatedData);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Registro no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al actualizar: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =====================================================
    // DELETE - Eliminar registro por ID
    // =====================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSensorData(@PathVariable Long id) {
        try {
            boolean deleted = sensorDataService.deleteSensorData(id);
            
            Map<String, Object> response = new HashMap<>();
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Registro eliminado");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Registro no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al eliminar: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =====================================================
    // ESTADÍSTICAS
    // =====================================================
    @GetMapping("/stats/total-records")
    public ResponseEntity<Map<String, Object>> getTotalRecords() {
        try {
            Long total = sensorDataService.getTotalRecords();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total_records", total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/stats/temperature-max")
    public ResponseEntity<Map<String, Object>> getMaxTemperature(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);
            
            Double maxTemp = sensorDataService.getMaxTemperatureInRange(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("max_temperature", maxTemp);
            response.put("start_date", startDate);
            response.put("end_date", endDate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/stats/humidity-avg")
    public ResponseEntity<Map<String, Object>> getAverageHumidity(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);
            
            Double avgHumidity = sensorDataService.getAverageHumidityInRange(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("average_humidity", avgHumidity);
            response.put("start_date", startDate);
            response.put("end_date", endDate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // =====================================================
    // HEALTH CHECK
    // =====================================================
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API está funcionando correctamente");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
