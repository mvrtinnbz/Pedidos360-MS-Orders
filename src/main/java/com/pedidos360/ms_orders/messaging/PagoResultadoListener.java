package com.pedidos360.ms_orders.messaging;

import com.pedidos360.ms_orders.config.RabbitMQConfig;
import com.pedidos360.ms_orders.event.PagoResultadoEvent;
import com.pedidos360.ms_orders.service.OrdenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Escucha el resultado de los pagos que publica ms-pago.
@Component
public class PagoResultadoListener {

    private static final Logger log = LoggerFactory.getLogger(PagoResultadoListener.class);

    private final OrdenService ordenService;

    public PagoResultadoListener(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDENES_PAGOS)
    public void recibir(PagoResultadoEvent evento) {

        log.info("Resultado de pago recibido -> orden {} : {}", evento.getOrdenId(), evento.getEstado());

        // try/catch para que un error no deje el mensaje reintentandose infinitamente
        try {
            ordenService.procesarResultadoPago(evento);
        } catch (Exception e) {
            log.error("Error procesando el pago {} de la orden {}: {}",
                    evento.getPagoId(), evento.getOrdenId(), e.getMessage());
        }
    }
}