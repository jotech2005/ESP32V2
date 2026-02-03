package com.proyecto.service;

import com.proyecto.dto.RFIDAuthRequestDTO;
import com.proyecto.dto.RFIDAuthResponseDTO;
import com.proyecto.model.RFIDAcceso;
import com.proyecto.repository.RFIDAccesoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RFIDAuthService {

    private final RFIDAccesoRepository rfidAccesoRepository;

    public RFIDAuthService(RFIDAccesoRepository rfidAccesoRepository) {
        this.rfidAccesoRepository = rfidAccesoRepository;
    }

    /**
     * Verificar o registrar tarjeta con PIN
     * Si la tarjeta no existe: crear nuevo registro con el PIN
     * Si la tarjeta existe: verificar que el PIN coincida
     */
    public RFIDAuthResponseDTO verificarORegistrarPIN(RFIDAuthRequestDTO request) {
        String rfidTag = request.getRfidTag();
        String pinIngresado = request.getPinIngresado();

        // Buscar si la tarjeta existe
        Optional<RFIDAcceso> accesoExistente = rfidAccesoRepository.findByRfidTag(rfidTag);

        if (accesoExistente.isEmpty()) {
            // Es una tarjeta NUEVA - registrar con el PIN ingresado
            RFIDAcceso nuevoAcceso = new RFIDAcceso();
            nuevoAcceso.setRfidTag(rfidTag);
            nuevoAcceso.setPinAcceso(pinIngresado);
            nuevoAcceso.setContadorAccesos(1);
            nuevoAcceso.setActivo(true);
            
            rfidAccesoRepository.save(nuevoAcceso);
            
            return new RFIDAuthResponseDTO(
                true,
                true,
                "Tarjeta registrada con PIN exitosamente",
                rfidTag
            );
        } else {
            // La tarjeta EXISTE - verificar PIN
            RFIDAcceso acceso = accesoExistente.get();
            
            if (!acceso.getActivo()) {
                return new RFIDAuthResponseDTO(
                    false,
                    false,
                    "Tarjeta desactivada",
                    rfidTag
                );
            }
            
            // Comparar PIN
            if (acceso.getPinAcceso() != null && acceso.getPinAcceso().equals(pinIngresado)) {
                // PIN correcto
                acceso.setUltimoAcceso(LocalDateTime.now());
                acceso.setContadorAccesos(acceso.getContadorAccesos() + 1);
                rfidAccesoRepository.save(acceso);
                
                return new RFIDAuthResponseDTO(
                    true,
                    false,
                    "Acceso autorizado",
                    rfidTag
                );
            } else {
                // PIN incorrecto
                return new RFIDAuthResponseDTO(
                    false,
                    false,
                    "PIN incorrecto",
                    rfidTag
                );
            }
        }
    }

    /**
     * Obtener información de una tarjeta RFID
     */
    public Optional<RFIDAcceso> obtenerAccesoPorRFID(String rfidTag) {
        return rfidAccesoRepository.findByRfidTag(rfidTag);
    }

    /**
     * Cambiar PIN de una tarjeta existente
     */
    public RFIDAuthResponseDTO cambiarPIN(String rfidTag, String pinAnterior, String pinNuevo) {
        Optional<RFIDAcceso> accesoOpt = rfidAccesoRepository.findByRfidTag(rfidTag);

        if (accesoOpt.isEmpty()) {
            return new RFIDAuthResponseDTO(false, false, "Tarjeta no encontrada", rfidTag);
        }

        RFIDAcceso acceso = accesoOpt.get();

        // Verificar PIN anterior
        if (acceso.getPinAcceso() == null || !acceso.getPinAcceso().equals(pinAnterior)) {
            return new RFIDAuthResponseDTO(false, false, "PIN anterior incorrecto", rfidTag);
        }

        // Cambiar PIN
        acceso.setPinAcceso(pinNuevo);
        rfidAccesoRepository.save(acceso);

        return new RFIDAuthResponseDTO(true, false, "PIN actualizado correctamente", rfidTag);
    }

    /**
     * Desactivar una tarjeta
     */
    public RFIDAuthResponseDTO desactivarTarjeta(String rfidTag) {
        Optional<RFIDAcceso> accesoOpt = rfidAccesoRepository.findByRfidTag(rfidTag);

        if (accesoOpt.isEmpty()) {
            return new RFIDAuthResponseDTO(false, false, "Tarjeta no encontrada", rfidTag);
        }

        RFIDAcceso acceso = accesoOpt.get();
        acceso.setActivo(false);
        rfidAccesoRepository.save(acceso);

        return new RFIDAuthResponseDTO(false, false, "Tarjeta desactivada", rfidTag);
    }
}
