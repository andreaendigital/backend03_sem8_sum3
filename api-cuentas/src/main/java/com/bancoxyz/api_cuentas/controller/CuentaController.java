package com.bancoxyz.api_cuentas.controller;

import com.bancoxyz.api_cuentas.dto.CuentaResumenDTO;
import com.bancoxyz.api_cuentas.kafka.TransaccionEventoDTO;
import com.bancoxyz.api_cuentas.kafka.TransaccionEventoProducer;
import com.bancoxyz.api_cuentas.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService service;
    private final TransaccionEventoProducer eventoProducer;

    @Autowired
    public CuentaController(CuentaService service, TransaccionEventoProducer eventoProducer) {
        this.service = service;
        this.eventoProducer = eventoProducer;
    }

    // ---------------------------------------------------------------
    // Endpoints existentes (Semana 6) — sin modificaciones
    // ---------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<CuentaResumenDTO>> obtenerTodas() {
        List<CuentaResumenDTO> cuentas = service.obtenerTodas();
        return new ResponseEntity<>(cuentas, HttpStatus.OK);
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<CuentaResumenDTO> obtenerPorId(@PathVariable Long cuentaId) {
        CuentaResumenDTO cuenta = service.obtenerPorId(cuentaId);
        return new ResponseEntity<>(cuenta, HttpStatus.OK);
    }

    // ---------------------------------------------------------------
    // Endpoint nuevo (Semana 7) — publica evento Kafka
    // ---------------------------------------------------------------

    /**
     * Consulta los movimientos reales de una cuenta desde annual_statements
     * (procesados por Spring Batch en Semana 3) y publica un evento Kafka
     * en el topic "transacciones-eventos".
     *
     * Requiere JWT válido (mismo mecanismo que los endpoints existentes).
     * No body requerido — el cuentaId viaja en el path.
     *
     * Responde 202 Accepted con el evento publicado como confirmación.
     */
    @PostMapping("/{cuentaId}/transacciones/evento")
    public ResponseEntity<TransaccionEventoDTO> publicarEventoTransacciones(
            @PathVariable Long cuentaId) {

        TransaccionEventoDTO evento = eventoProducer.publicarEventoTransacciones(cuentaId);
        return new ResponseEntity<>(evento, HttpStatus.ACCEPTED);
    }
}