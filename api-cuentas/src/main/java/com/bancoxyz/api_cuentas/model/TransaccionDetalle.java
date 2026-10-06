package com.bancoxyz.api_cuentas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA que mapea la tabla annual_statements generada por el
 * proyecto Spring Batch (Semana 3). Contiene los movimientos individuales
 * de cada cuenta procesados desde cuentas_anuales.csv.
 *
 * La tabla es de solo lectura desde api-cuentas (ddl-auto=none).
 */
@Entity
@Table(name = "annual_statements")
public class TransaccionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    @Column(name = "fecha")
    private LocalDate fechaTransaccion;

    /**
     * Tipo de movimiento calculado por el processor de Semana 3:
     * "ingreso" (monto >= 0) o "retiro" (monto < 0).
     */
    @Column(name = "transaccion")
    private String tipoTransaccion;

    @Column(name = "monto")
    private BigDecimal monto;

    @Column(name = "descripcion")
    private String descripcion;

    // Constructor vacío requerido por JPA
    public TransaccionDetalle() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public LocalDate getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(LocalDate fechaTransaccion) { this.fechaTransaccion = fechaTransaccion; }

    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
