package com.proyecto.controller;

import com.proyecto.dto.RFIDAuthRequestDTO;
import com.proyecto.dto.RFIDAuthResponseDTO;
import com.proyecto.service.RFIDAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rfid-auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RFIDAuthController {

    private final RFIDAuthService rfidAuthService;

    public RFIDAuthController(RFIDAuthService rfidAuthService) {
        this.rfidAuthService = rfidAuthService;
    }

    /**
     * Verificar o registrar tarjeta con PIN
     * POST /api/rfid-auth
     * 
     * Request:
     * {
     *   "rfid_tag": "1A2B3C4D",
     *   "pin_ingresado": "1234",
     *   "accion": "verificar_pin"
     * }
     * 
     * Response (éxito):
     * {
     *   "autenticado": true,
     *   "es_nueva": true/false,
     *   "mensaje": "Tarjeta registrada con PIN exitosamente" o "Acceso autorizado",
     *   "rfid_tag": "1A2B3C4D"
     * }
     */
    @PostMapping
    public ResponseEntity<RFIDAuthResponseDTO> verificarPIN(@RequestBody RFIDAuthRequestDTO request) {
        try {
            RFIDAuthResponseDTO response = rfidAuthService.verificarORegistrarPIN(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RFIDAuthResponseDTO(false, false, "Error: " + e.getMessage(), ""));
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
            return rfidAuthService.obtenerAccesoPorRFID(rfidTag)
                    .map(acceso -> ResponseEntity.ok()
                            .body(new java.util.HashMap<String, Object>() {{
                                put("encontrada", true);
                                put("rfid_tag", acceso.getRfidTag());
                                put("nombre_usuario", acceso.getNombreUsuario());
                                put("activo", acceso.getActivo());
                                put("contador_accesos", acceso.getContadorAccesos());
                                put("ultimo_acceso", acceso.getUltimoAcceso());
                            }}))
                    .orElseGet(() -> ResponseEntity.ok()
                            .body(new java.util.HashMap<String, Object>() {{
                                put("encontrada", false);
                                put("mensaje", "Tarjeta no encontrada");
                            }}));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new java.util.HashMap<String, String>() {{
                        put("error", e.getMessage());
                    }});
        }
    }
}
