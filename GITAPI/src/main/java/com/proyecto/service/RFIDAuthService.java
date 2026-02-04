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
     * Registrar paso de tarjeta (SIN PIN)
     * Lógica toggle:
     * - Pases impares (1, 3, 5...): "Hola" (entrada)
     * - Pases pares (2, 4, 6...): "Adios" (salida)
     */
    public RFIDAuthResponseDTO verificarORegistrarPIN(RFIDAuthRequestDTO request) {
        try {
            String rfidTag = request.getRfidTag();
            System.out.println("[SERVICE] Procesando RFID: " + rfidTag);

            // Buscar si la tarjeta existe
            Optional<RFIDAcceso> accesoOpt = rfidAccesoRepository.findByRfidTag(rfidTag);
            LocalDateTime ahora = LocalDateTime.now();
            
            if (accesoOpt.isEmpty()) {
                // Tarjeta NUEVA - Primera vez = Entrada
                System.out.println("[SERVICE] Tarjeta nueva, registrando primera entrada");
                
                RFIDAcceso nuevoAcceso = new RFIDAcceso();
                nuevoAcceso.setRfidTag(rfidTag);
                nuevoAcceso.setPinAcceso("");
                nuevoAcceso.setActivo(true);
                nuevoAcceso.setContadorAccesos(1); // Primer acceso
                nuevoAcceso.setUltimoAcceso(ahora);
                
                rfidAccesoRepository.save(nuevoAcceso);
                
                return new RFIDAuthResponseDTO(true, true, "Hola", rfidTag);
            } else {
                // Tarjeta EXISTENTE - Alternar Hola/Adios
                RFIDAcceso acceso = accesoOpt.get();
                
                // Obtener contador actual (si es null, empezar en 0)
                int contador = acceso.getContadorAccesos() != null ? acceso.getContadorAccesos() : 0;
                
                // Incrementar contador
                contador++;
                
                // Determinar mensaje: impar=Hola (entrada), par=Adios (salida)
                String mensaje = (contador % 2 == 1) ? "Hola" : "Adios";
                
                System.out.println("[SERVICE] Contador: " + contador + " -> " + mensaje);
                
                // Actualizar registro
                acceso.setContadorAccesos(contador);
                acceso.setUltimoAcceso(ahora);
                rfidAccesoRepository.save(acceso);
                
                return new RFIDAuthResponseDTO(true, false, mensaje, rfidTag);
            }
        } catch (Exception e) {
            System.err.println("[SERVICE] ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error procesando RFID: " + e.getMessage(), e);
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
