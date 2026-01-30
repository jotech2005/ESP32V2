package com.proyecto.repository;

import com.proyecto.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // Buscar por último RFID
    List<SensorData> findByUltimaTarjetaRfidOrderByFechaCreacionDesc(String ultimaTarjetaRfid);

    // Buscar por rango de fecha
    List<SensorData> findByFechaCreacionBetweenOrderByFechaCreacionDesc(
            LocalDateTime inicio, LocalDateTime fin
    );

    // Buscar por temperatura mayor a X
    List<SensorData> findByTemperaturaGreaterThanOrderByFechaCreacionDesc(Double temperatura);

    // Buscar por humedad menor a X
    List<SensorData> findByHumedadLessThanOrderByFechaCreacionDesc(Double humedad);

    // Buscar todos ordenados por fecha descendente
    List<SensorData> findAllByOrderByFechaCreacionDesc();

    // Query personalizada para obtener últimas N lecturas
    @Query(value = "SELECT * FROM sensor_datos ORDER BY fecha_creacion DESC LIMIT :limit", 
           nativeQuery = true)
    List<SensorData> findLastNReadings(@Param("limit") int limit);

    // Query personalizada para contar registros por IP
    @Query("SELECT COUNT(s) FROM SensorData s WHERE s.esp32Ip = :ip")
    Long countByEsp32Ip(@Param("ip") String ip);

    // Buscar registros con luz detectada
    List<SensorData> findByLuzDetectadaTrueOrderByFechaCreacionDesc();

    // Encontrar máxima temperatura en un período
    @Query("SELECT MAX(s.temperatura) FROM SensorData s WHERE s.fechaCreacion BETWEEN :inicio AND :fin")
    Double findMaxTemperaturaInRange(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Encontrar promedio de humedad en un período
    @Query("SELECT AVG(s.humedad) FROM SensorData s WHERE s.fechaCreacion BETWEEN :inicio AND :fin")
    Double findAverageHumedadInRange(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
