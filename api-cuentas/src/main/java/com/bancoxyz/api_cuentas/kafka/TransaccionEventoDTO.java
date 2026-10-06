package com.bancoxyz.api_cuentas.kafka;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO del evento publicado en el topic Kafka "transacciones-eventos".
 *
 * Se publica cuando un cliente consulta los movimientos reales de una cuenta
 * (datos procesados por Spring Batch en Semana 3, tabla annual_statements).
 *
 * Estructura JSON resultante:
 * {
 *   "tipoEvento": "TRANSACCIONES_CUENTA_CONSULTADAS",
 *   "cuentaId": 103,
 *   "fechaEvento": "2026-10-05T14:30:00",
 *   "totalMovimientos": 3,
 *   "movimientos": [ { "fecha": "...", "tipoTransaccion": "...", "monto": ..., "descripcion": "..." } ]
 * }
 */
public class TransaccionEventoDTO {

    private String tipoEvento;
    private Long cuentaId;
    private LocalDateTime fechaEvento;
    private int totalMovimientos;
    private List<MovimientoDTO> movimientos;

    public TransaccionEventoDTO() {}

    public TransaccionEventoDTO(Long cuentaId, List<MovimientoDTO> movimientos) {
        this.tipoEvento = "TRANSACCIONES_CUENTA_CONSULTADAS";
        this.cuentaId = cuentaId;
        this.fechaEvento = LocalDateTime.now();
        this.movimientos = movimientos;
        this.totalMovimientos = movimientos != null ? movimientos.size() : 0;
    }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }

    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }

    public int getTotalMovimientos() { return totalMovimientos; }
    public void setTotalMovimientos(int totalMovimientos) { this.totalMovimientos = totalMovimientos; }

    public List<MovimientoDTO> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientoDTO> movimientos) { this.movimientos = movimientos; }
}
