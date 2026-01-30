package com.proyecto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_datos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long timestamp;

    @Column(name = "temperatura", nullable = false)
    private Double temperatura;

    @Column(name = "humedad", nullable = false)
    private Double humedad;

    @Column(name = "luz_detectada", nullable = false)
    private Boolean luzDetectada;

    @Column(name = "teclado_input", length = 255)
    private String tecladoInput;

    @Column(name = "ultima_tarjeta_rfid", length = 20)
    private String ultimaTarjetaRfid;

    @Column(name = "esp32_ip", length = 20)
    private String esp32Ip;

    @Column(name = "rssi")
    private Integer rssi;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
