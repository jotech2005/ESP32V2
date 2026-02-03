package com.proyecto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RFIDAuthRequestDTO {
    
    @JsonProperty("rfid_tag")
    private String rfidTag;
    
    @JsonProperty("pin_ingresado")
    private String pinIngresado;
    
    @JsonProperty("accion")
    private String accion;
    
    public RFIDAuthRequestDTO() {
    }
    
    public RFIDAuthRequestDTO(String rfidTag, String pinIngresado, String accion) {
        this.rfidTag = rfidTag;
        this.pinIngresado = pinIngresado;
        this.accion = accion;
    }
    
    public String getRfidTag() {
        return rfidTag;
    }
    
    public void setRfidTag(String rfidTag) {
        this.rfidTag = rfidTag;
    }
    
    public String getPinIngresado() {
        return pinIngresado;
    }
    
    public void setPinIngresado(String pinIngresado) {
        this.pinIngresado = pinIngresado;
    }
    
    public String getAccion() {
        return accion;
    }
    
    public void setAccion(String accion) {
        this.accion = accion;
    }
}
