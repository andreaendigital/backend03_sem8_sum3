package com.bancoxyz.api_cuentas.repository;

import com.bancoxyz.api_cuentas.model.TransaccionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de solo lectura sobre la tabla annual_statements.
 * Los datos fueron escritos por el job Spring Batch de Semana 3;
 * este microservicio solo los consulta.
 */
@Repository
public interface TransaccionDetalleRepository extends JpaRepository<TransaccionDetalle, Long> {

    /**
     * Devuelve todos los movimientos registrados para una cuenta.
     * Usado por el producer Kafka para construir el evento.
     */
    List<TransaccionDetalle> findByCuentaId(Long cuentaId);
}
