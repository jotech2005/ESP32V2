package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class TelemetryDTO {

    @NotBlank
    private String datetime;

    @NotBlank
    private String uid;

    @Min(0)
    @Max(1)
    private Integer luz;

    private Double temp;
    private Double hum;

    public String getDatetime() { return datetime; }
    public void setDatetime(String datetime) { this.datetime = datetime; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public Integer getLuz() { return luz; }
    public void setLuz(Integer luz) { this.luz = luz; }

    public Double getTemp() { return temp; }
    public void setTemp(Double temp) { this.temp = temp; }

    public Double getHum() { return hum; }
    public void setHum(Double hum) { this.hum = hum; }
}
