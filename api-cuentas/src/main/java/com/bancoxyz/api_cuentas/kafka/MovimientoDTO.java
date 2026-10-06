package com.bancoxyz.api_cuentas.kafka;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa un movimiento individual dentro del evento Kafka.
 * Los campos provienen directamente de la tabla annual_statements
 * (proyecto Spring Batch, Semana 3).
 */
public class MovimientoDTO {

    private LocalDate fecha;
    private String tipoTransaccion; // "ingreso" o "retiro"
    private BigDecimal monto;
    private String descripcion;

    public MovimientoDTO() {}

    public MovimientoDTO(LocalDate fecha, String tipoTransaccion,
                         BigDecimal monto, String descripcion) {
        this.fecha = fecha;
        this.tipoTransaccion = tipoTransaccion;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
