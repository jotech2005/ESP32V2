package com.proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir datos del ESP32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataDTO {
    
    private Long timestamp;
    private Double temperatura;
    private Double humedad;
    private Boolean luz_detectada;
    private String teclado_input;
    private String ultima_tarjeta_rfid;
    private String esp32_ip;
    private Integer rssi;
}
