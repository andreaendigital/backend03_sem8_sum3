package com.bancoxyz.api_cuentas.kafka;

import com.bancoxyz.api_cuentas.model.TransaccionDetalle;
import com.bancoxyz.api_cuentas.repository.TransaccionDetalleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Servicio productor Kafka.
 *
 * Consulta los movimientos reales de una cuenta desde annual_statements
 * (generados por Spring Batch en Semana 3), construye el evento
 * TransaccionEventoDTO y lo publica en el topic "transacciones-eventos".
 */
@Service
public class TransaccionEventoProducer {

    private static final Logger log = LoggerFactory.getLogger(TransaccionEventoProducer.class);

    private final KafkaTemplate<String, TransaccionEventoDTO> kafkaTemplate;
    private final TransaccionDetalleRepository transaccionDetalleRepository;

    @Value("${banco.kafka.topic.transacciones}")
    private String topic;

    public TransaccionEventoProducer(KafkaTemplate<String, TransaccionEventoDTO> kafkaTemplate,
                                     TransaccionDetalleRepository transaccionDetalleRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.transaccionDetalleRepository = transaccionDetalleRepository;
    }

    /**
     * Consulta los movimientos de la cuenta en annual_statements,
     * construye el evento y lo publica en Kafka.
     *
     * @param cuentaId identificador de la cuenta
     * @return el evento publicado (para que el controller lo devuelva en la respuesta)
     */
    public TransaccionEventoDTO publicarEventoTransacciones(Long cuentaId) {

        // 1. Consultar movimientos reales desde annual_statements
        List<TransaccionDetalle> detalles = transaccionDetalleRepository.findByCuentaId(cuentaId);

        // 2. Convertir entidades a DTOs de movimiento
        List<MovimientoDTO> movimientos = detalles.stream()
                .map(d -> new MovimientoDTO(
                        d.getFechaTransaccion(),
                        d.getTipoTransaccion(),
                        d.getMonto(),
                        d.getDescripcion()))
                .collect(Collectors.toList());

        // 3. Construir el evento
        TransaccionEventoDTO evento = new TransaccionEventoDTO(cuentaId, movimientos);

        // 4. Publicar en Kafka de forma asíncrona; loguear confirmación o error
        log.info("[Kafka Producer] Publicando evento | tipo={} | cuentaId={} | movimientos={}",
                evento.getTipoEvento(), evento.getCuentaId(), evento.getTotalMovimientos());

        CompletableFuture<SendResult<String, TransaccionEventoDTO>> future =
                kafkaTemplate.send(topic, String.valueOf(cuentaId), evento);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[Kafka Producer] Error al publicar evento para cuentaId={} : {}",
                        cuentaId, ex.getMessage());
            } else {
                log.info("[Kafka Producer] Evento publicado correctamente | topic={} | partition={} | offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return evento;
    }
}
