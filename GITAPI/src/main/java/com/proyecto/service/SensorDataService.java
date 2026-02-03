package com.proyecto.service;

import com.proyecto.model.SensorData;
import com.proyecto.repository.SensorDataRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository;

    public SensorDataService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    // =====================================================
    // CREATE - Guardar nuevos datos
    // =====================================================
    public SensorData saveSensorData(SensorData sensorData) {
        return sensorDataRepository.save(sensorData);
    }

    // =====================================================
    // READ - Obtener datos
    // =====================================================
    public Optional<SensorData> getSensorDataById(Long id) {
        return sensorDataRepository.findById(id);
    }

    public List<SensorData> getAllSensorData() {
        return sensorDataRepository.findAllByOrderByFechaCreacionDesc();
    }

    // ✅ FIX: últimas N lecturas (sin LIMIT :limit)
    public List<SensorData> getLastNReadings(int limit) {
        if (limit <= 0) return List.of();
        return sensorDataRepository.findLastReadings(PageRequest.of(0, limit));
    }

    public List<SensorData> getDataByRfidTag(String rfidTag) {
        return sensorDataRepository.findByUltimaTarjetaRfidOrderByFechaCreacionDesc(rfidTag);
    }

    public List<SensorData> getDataByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return sensorDataRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(startDate, endDate);
    }

    public List<SensorData> getDataWithHighTemperature(Double temperature) {
        return sensorDataRepository.findByTemperaturaGreaterThanOrderByFechaCreacionDesc(temperature);
    }

    public List<SensorData> getDataWithLowHumidity(Double humidity) {
        return sensorDataRepository.findByHumedadLessThanOrderByFechaCreacionDesc(humidity);
    }

    public List<SensorData> getDataWithLightDetected() {
        return sensorDataRepository.findByLuzDetectadaTrueOrderByFechaCreacionDesc();
    }

    // =====================================================
    // UPDATE - Actualizar datos
    // =====================================================
    public Optional<SensorData> updateSensorData(Long id, SensorData updatedData) {
        return sensorDataRepository.findById(id).map(data -> {

            if (updatedData.getTemperatura() != null) {
                data.setTemperatura(updatedData.getTemperatura());
            }
            if (updatedData.getHumedad() != null) {
                data.setHumedad(updatedData.getHumedad());
            }
            if (updatedData.getLuzDetectada() != null) {
                data.setLuzDetectada(updatedData.getLuzDetectada());
            }
            if (updatedData.getTecladoInput() != null) {
                data.setTecladoInput(updatedData.getTecladoInput());
            }
            if (updatedData.getUltimaTarjetaRfid() != null) {
                data.setUltimaTarjetaRfid(updatedData.getUltimaTarjetaRfid());
            }

            return sensorDataRepository.save(data);
        });
    }

    // =====================================================
    // DELETE - Eliminar datos
    // =====================================================
    public boolean deleteSensorData(Long id) {
        if (!sensorDataRepository.existsById(id)) return false;
        sensorDataRepository.deleteById(id);
        return true;
    }

    public void deleteAllSensorData() {
        sensorDataRepository.deleteAll();
    }

    // =====================================================
    // ANÁLISIS Y ESTADÍSTICAS
    // =====================================================
    public Double getMaxTemperatureInRange(LocalDateTime startDate, LocalDateTime endDate) {
        return sensorDataRepository.findMaxTemperaturaInRange(startDate, endDate);
    }

    public Double getAverageHumidityInRange(LocalDateTime startDate, LocalDateTime endDate) {
        return sensorDataRepository.findAverageHumedadInRange(startDate, endDate);
    }

    public Long countRecordsByIp(String ip) {
        return sensorDataRepository.countByEsp32Ip(ip);
    }

    public Long getTotalRecords() {
        return sensorDataRepository.count();
    }
}
