-- =====================================================
-- SCRIPT DE VERIFICACIÓN Y PRUEBAS
-- Tabla: rfid_accesos
-- =====================================================

-- 1. VER TODAS LAS TARJETAS REGISTRADAS
SELECT 
    id,
    rfid_tag,
    nombre_usuario,
    pin_acceso,
    contador_accesos,
    activo,
    fecha_creacion,
    ultimo_acceso
FROM rfid_accesos
ORDER BY fecha_creacion DESC;

-- 2. BUSCAR TARJETA ESPECÍFICA
-- Cambiar '1A2B3C4D' por el RFID que estés probando
SELECT * FROM rfid_accesos 
WHERE rfid_tag = '1A2B3C4D';

-- 3. VER TARJETAS ACTIVAS
SELECT 
    rfid_tag,
    nombre_usuario,
    contador_accesos,
    ultimo_acceso
FROM rfid_accesos
WHERE activo = 1
ORDER BY ultimo_acceso DESC;

-- 4. VER TARJETAS INACTIVAS
SELECT 
    rfid_tag,
    nombre_usuario,
    fecha_creacion,
    ultimo_acceso
FROM rfid_accesos
WHERE activo = 0;

-- 5. CONTAR TOTAL DE TARJETAS
SELECT COUNT(*) as total_tarjetas FROM rfid_accesos;

-- 6. VER TARJETAS POR ACTIVIDAD (Últimos 7 días)
SELECT 
    rfid_tag,
    contador_accesos,
    ultimo_acceso,
    DATEDIFF(NOW(), ultimo_acceso) as dias_sin_usar
FROM rfid_accesos
WHERE ultimo_acceso >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY ultimo_acceso DESC;

-- 7. CONTAR ACCESOS TOTALES
SELECT 
    rfid_tag,
    contador_accesos,
    ultimo_acceso
FROM rfid_accesos
ORDER BY contador_accesos DESC;

-- 8. ACTUALIZAR NOMBRE DE USUARIO (Opcional)
-- UPDATE rfid_accesos 
-- SET nombre_usuario = 'Mi Tarjeta'
-- WHERE rfid_tag = '1A2B3C4D';

-- 9. CAMBIAR PIN (Opcional)
-- UPDATE rfid_accesos 
-- SET pin_acceso = '5678'
-- WHERE rfid_tag = '1A2B3C4D';

-- 10. DESACTIVAR TARJETA (Opcional)
-- UPDATE rfid_accesos 
-- SET activo = 0
-- WHERE rfid_tag = '1A2B3C4D';

-- 11. REACTIVAR TARJETA (Opcional)
-- UPDATE rfid_accesos 
-- SET activo = 1
-- WHERE rfid_tag = '1A2B3C4D';

-- 12. ELIMINAR TARJETA (Opcional - CUIDADO!)
-- DELETE FROM rfid_accesos 
-- WHERE rfid_tag = '1A2B3C4D';

-- 13. LIMPIAR TODA LA TABLA (Opcional - CUIDADO!)
-- DELETE FROM rfid_accesos;

-- 14. RESETEAR AUTO_INCREMENT (después de limpiar)
-- ALTER TABLE rfid_accesos AUTO_INCREMENT = 1;

-- 15. VER ESTRUCTURA DE TABLA
DESCRIBE rfid_accesos;

-- 16. VER ÍNDICES
SHOW INDEXES FROM rfid_accesos;

-- 17. VER TAMAÑO DE LA TABLA
SELECT 
    table_name,
    ROUND(((data_length + index_length) / 1024 / 1024), 2) as size_mb
FROM information_schema.TABLES
WHERE table_schema = 'esp32_iot'
AND table_name = 'rfid_accesos';

-- 18. ESTADÍSTICAS GENERALES
SELECT 
    COUNT(*) as total_tarjetas,
    SUM(contador_accesos) as accesos_totales,
    AVG(contador_accesos) as promedio_accesos,
    MAX(contador_accesos) as max_accesos,
    MIN(contador_accesos) as min_accesos,
    SUM(IF(activo = 1, 1, 0)) as tarjetas_activas,
    SUM(IF(activo = 0, 1, 0)) as tarjetas_inactivas
FROM rfid_accesos;

-- 19. TARJETAS CON MÁS ACCESOS
SELECT 
    rfid_tag,
    nombre_usuario,
    contador_accesos,
    ultimo_acceso
FROM rfid_accesos
ORDER BY contador_accesos DESC
LIMIT 10;

-- 20. ÚLTIMOS ACCESOS (Auditoría)
SELECT 
    rfid_tag,
    nombre_usuario,
    contador_accesos,
    ultimo_acceso,
    DATE_FORMAT(ultimo_acceso, '%Y-%m-%d %H:%i:%s') as fecha_hora
FROM rfid_accesos
ORDER BY ultimo_acceso DESC
LIMIT 20;
