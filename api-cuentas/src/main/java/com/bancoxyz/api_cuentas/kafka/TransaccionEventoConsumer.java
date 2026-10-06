package com.bancoxyz.api_cuentas.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor Kafka del topic "transacciones-eventos".
 *
 * Escucha los eventos TRANSACCIONES_CUENTA_CONSULTADAS y deja evidencia
 * en los logs de la aplicación. En una arquitectura real podría
 * derivar el evento a un servicio de auditoría o notificaciones;
 * para Semana 7 el log es suficiente para demostrar Producer → Kafka → Consumer.
 */
@Component
public class TransaccionEventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransaccionEventoConsumer.class);

    /**
     * Escucha el topic configurado en banco.kafka.topic.transacciones.
     * El containerFactory apunta al ConcurrentKafkaListenerContainerFactory definido
     * en KafkaConsumerConfig.
     */
    @KafkaListener(
            topics = "${banco.kafka.topic.transacciones}",
            groupId = "api-cuentas-grupo",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void escuchar(TransaccionEventoDTO evento) {
        log.info("============================================================");
        log.info("[Kafka Consumer] Evento recibido");
        log.info("[Kafka Consumer] tipo          = {}", evento.getTipoEvento());
        log.info("[Kafka Consumer] cuentaId      = {}", evento.getCuentaId());
        log.info("[Kafka Consumer] fechaEvento   = {}", evento.getFechaEvento());
        log.info("[Kafka Consumer] movimientos   = {}", evento.getTotalMovimientos());

        if (evento.getMovimientos() != null && !evento.getMovimientos().isEmpty()) {
            log.info("[Kafka Consumer] Detalle de movimientos:");
            evento.getMovimientos().forEach(m ->
                log.info("[Kafka Consumer]   fecha={} | tipo={} | monto={} | desc={}",
                        m.getFecha(),
                        m.getTipoTransaccion(),
                        m.getMonto(),
                        m.getDescripcion())
            );
        } else {
            log.info("[Kafka Consumer] Sin movimientos registrados para esta cuenta.");
        }

        log.info("============================================================");
    }
}
