package com.proyecto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RFIDAuthResponseDTO {
    
    @JsonProperty("autenticado")
    private boolean autenticado;
    
    @JsonProperty("es_nueva")
    private boolean esNueva;
    
    @JsonProperty("mensaje")
    private String mensaje;
    
    @JsonProperty("rfid_tag")
    private String rfidTag;
    
    public RFIDAuthResponseDTO() {
    }
    
    public RFIDAuthResponseDTO(boolean autenticado, boolean esNueva, String mensaje, String rfidTag) {
        this.autenticado = autenticado;
        this.esNueva = esNueva;
        this.mensaje = mensaje;
        this.rfidTag = rfidTag;
    }
    
    public boolean isAutenticado() {
        return autenticado;
    }
    
    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }
    
    public boolean isEsNueva() {
        return esNueva;
    }
    
    public void setEsNueva(boolean esNueva) {
        this.esNueva = esNueva;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public String getRfidTag() {
        return rfidTag;
    }
    
    public void setRfidTag(String rfidTag) {
        this.rfidTag = rfidTag;
    }
}
