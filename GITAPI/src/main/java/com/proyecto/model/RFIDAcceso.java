package com.proyecto.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rfid_accesos")
public class RFIDAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("rfid_tag")
    @JsonAlias("rfid_tag")
    @Column(name = "rfid_tag", unique = true, length = 20, nullable = false)
    private String rfidTag;

    @JsonProperty("nombre_usuario")
    @JsonAlias({"nombre_usuario", "nombreUsuario"})
    @Column(name = "nombre_usuario", length = 100)
    private String nombreUsuario;

    @JsonProperty("pin_acceso")
    @JsonAlias({"pin_acceso", "pinAcceso"})
    @Column(name = "pin_acceso", length = 10)
    private String pinAcceso;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "contador_accesos")
    private Integer contadorAccesos = 1;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.ultimoAcceso = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ultimoAcceso = LocalDateTime.now();
    }

    // Constructores
    public RFIDAcceso() {
    }

    public RFIDAcceso(String rfidTag, String pinAcceso) {
        this.rfidTag = rfidTag;
        this.pinAcceso = pinAcceso;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRfidTag() {
        return rfidTag;
    }

    public void setRfidTag(String rfidTag) {
        this.rfidTag = rfidTag;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPinAcceso() {
        return pinAcceso;
    }

    public void setPinAcceso(String pinAcceso) {
        this.pinAcceso = pinAcceso;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Integer getContadorAccesos() {
        return contadorAccesos;
    }

    public void setContadorAccesos(Integer contadorAccesos) {
        this.contadorAccesos = contadorAccesos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
