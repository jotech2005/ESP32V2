package com.proyecto.controller;

import com.proyecto.dto.RFIDAuthRequestDTO;
import com.proyecto.dto.RFIDAuthResponseDTO;
import com.proyecto.service.RFIDAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rfid-auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RFIDAuthController {

    private final RFIDAuthService rfidAuthService;

    public RFIDAuthController(RFIDAuthService rfidAuthService) {
        this.rfidAuthService = rfidAuthService;
    }

    /**
     * Registrar paso de tarjeta (SIN PIN)
     * POST /api/rfid-auth
     * 
     * Request:
     * {
     *   "rfid_tag": "1A2B3C4D",
     *   "accion": "toggle"
     * }
     * 
     * Response (éxito):
     * {
     *   "autenticado": true,
     *   "es_nueva": true/false,
     *   "mensaje": "Adelante" o "Adios",
     *   "rfid_tag": "1A2B3C4D"
     * }
     */
    @PostMapping
    public ResponseEntity<RFIDAuthResponseDTO> verificarPIN(@RequestBody RFIDAuthRequestDTO request) {
        try {
            // Validaciones básicas
            if (request.getRfidTag() == null || request.getRfidTag().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RFIDAuthResponseDTO(false, false, "Error: RFID tag no puede estar vacío", ""));
            }

            System.out.println("[CONTROLLER] Recibido - RFID: " + request.getRfidTag());
            
            RFIDAuthResponseDTO response = rfidAuthService.verificarORegistrarPIN(request);
            
            // Si es nueva tarjeta, retornar 201 (Created)
            if (response.isEsNueva() && response.isAutenticado()) {
                System.out.println("[CONTROLLER] Nueva tarjeta registrada: " + request.getRfidTag());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
            
            System.out.println("[CONTROLLER] Respuesta: " + response.isAutenticado());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("[CONTROLLER] Error en verificarPIN: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RFIDAuthResponseDTO(false, false, "Error interno: " + e.getMessage(), ""));
        }
    }

    /**
     * Cambiar PIN de una tarjeta
     * PUT /api/rfid-auth/cambiar-pin
     * 
     * Request:
     * {
     *   "rfid_tag": "1A2B3C4D",
     *   "pin_anterior": "1234",
     *   "pin_nuevo": "5678"
     * }
     */
    @PutMapping("/cambiar-pin")
    public ResponseEntity<RFIDAuthResponseDTO> cambiarPIN(
            @RequestParam String rfidTag,
            @RequestParam String pinAnterior,
            @RequestParam String pinNuevo) {
        try {
            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RFIDAuthResponseDTO(false, false, "Error: RFID tag no puede estar vacío", ""));
            }
            if (pinAnterior == null || pinAnterior.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RFIDAuthResponseDTO(false, false, "Error: PIN anterior no puede estar vacío", rfidTag));
            }
            if (pinNuevo == null || pinNuevo.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RFIDAuthResponseDTO(false, false, "Error: PIN nuevo no puede estar vacío", rfidTag));
            }
            if (pinNuevo.length() < 4) {
                return ResponseEntity.badRequest()
                        .body(new RFIDAuthResponseDTO(false, false, "Error: PIN nuevo debe tener mínimo 4 dígitos", rfidTag));
            }
            RFIDAuthResponseDTO response = rfidAuthService.cambiarPIN(rfidTag, pinAnterior, pinNuevo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RFIDAuthResponseDTO(false, false, "Error: " + e.getMessage(), rfidTag));
        }
    }

    /**
     * Desactivar una tarjeta
     * DELETE /api/rfid-auth/{rfidTag}
     */
    @DeleteMapping("/{rfidTag}")
    public ResponseEntity<RFIDAuthResponseDTO> desactivarTarjeta(@PathVariable String rfidTag) {
        try {
            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RFIDAuthResponseDTO(false, false, "Error: RFID tag no puede estar vacío", ""));
            }
            RFIDAuthResponseDTO response = rfidAuthService.desactivarTarjeta(rfidTag);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RFIDAuthResponseDTO(false, false, "Error: " + e.getMessage(), rfidTag));
        }
    }

    /**
     * Obtener información de una tarjeta
     * GET /api/rfid-auth/{rfidTag}
     */
    @GetMapping("/{rfidTag}")
    public ResponseEntity<?> obtenerTarjeta(@PathVariable String rfidTag) {
        try {
            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "RFID tag no puede estar vacío");
                return ResponseEntity.badRequest().body(error);
            }
            return rfidAuthService.obtenerAccesoPorRFID(rfidTag)
                    .map(acceso -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("encontrada", true);
                        result.put("rfid_tag", acceso.getRfidTag());
                        result.put("nombre_usuario", acceso.getNombreUsuario());
                        result.put("activo", acceso.getActivo());
                        result.put("contador_accesos", acceso.getContadorAccesos());
                        result.put("ultimo_acceso", acceso.getUltimoAcceso());
                        return ResponseEntity.ok(result);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("encontrada", false);
                        result.put("mensaje", "Tarjeta no encontrada");
                        return ResponseEntity.ok(result);
                    });
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
