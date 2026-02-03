package com.proyecto.repository;

import com.proyecto.model.RFIDAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RFIDAccesoRepository extends JpaRepository<RFIDAcceso, Long> {
    
    // Buscar tarjeta RFID por UID
    Optional<RFIDAcceso> findByRfidTag(String rfidTag);
    
    // Verificar si existe una tarjeta
    boolean existsByRfidTag(String rfidTag);
}
