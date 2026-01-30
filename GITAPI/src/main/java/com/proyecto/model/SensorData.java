package com.proyecto.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_datos")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("timestamp")
    @JsonAlias("timestamp")
    @Column(nullable = false)
    private Long timestamp;

    @JsonProperty("temperatura")
    @JsonAlias("temperatura")
    @Column(name = "temperatura", nullable = false)
    private Double temperatura;

    @JsonProperty("humedad")
    @JsonAlias("humedad")
    @Column(name = "humedad", nullable = false)
    private Double humedad;

    @JsonProperty("luz_detectada")
    @JsonAlias({"luz_detectada", "luzDetectada"})
    @Column(name = "luz_detectada", nullable = false)
    private Boolean luzDetectada;

    @JsonProperty("teclado_input")
    @JsonAlias({"teclado_input", "tecladoInput"})
    @Column(name = "teclado_input", length = 255)
    private String tecladoInput;

    @JsonProperty("ultima_tarjeta_rfid")
    @JsonAlias({"ultima_tarjeta_rfid", "ultimaTarjetaRfid"})
    @Column(name = "ultima_tarjeta_rfid", length = 20)
    private String ultimaTarjetaRfid;

    @JsonProperty("esp32_ip")
    @JsonAlias({"esp32_ip", "esp32Ip"})
    @Column(name = "esp32_ip", length = 20)
    private String esp32Ip;

    @JsonProperty("rssi")
    @JsonAlias("rssi")
    @Column(name = "rssi")
    private Integer rssi;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public SensorData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public Boolean getLuzDetectada() {
        return luzDetectada;
    }

    public void setLuzDetectada(Boolean luzDetectada) {
        this.luzDetectada = luzDetectada;
    }

    public String getTecladoInput() {
        return tecladoInput;
    }

    public void setTecladoInput(String tecladoInput) {
        this.tecladoInput = tecladoInput;
    }

    public String getUltimaTarjetaRfid() {
        return ultimaTarjetaRfid;
    }

    public void setUltimaTarjetaRfid(String ultimaTarjetaRfid) {
        this.ultimaTarjetaRfid = ultimaTarjetaRfid;
    }

    public String getEsp32Ip() {
        return esp32Ip;
    }

    public void setEsp32Ip(String esp32Ip) {
        this.esp32Ip = esp32Ip;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
