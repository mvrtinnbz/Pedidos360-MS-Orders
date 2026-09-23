package com.pedidos360.ms_orders.messaging;

import com.pedidos360.ms_orders.config.RabbitMQConfig;
import com.pedidos360.ms_orders.event.OrdenCreadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// Publica el evento en el exchange. Desde aqui RabbitMQ lo reparte
// a las colas de productos (stock) y notificaciones (correo).
@Component
public class OrdenEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrdenEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public OrdenEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicarOrdenCreada(OrdenCreadaEvent evento) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_ORDEN_CREADA,
                evento
        );

        log.info("Evento publicado [{}] -> orden {} del usuario {}",
                RabbitMQConfig.ROUTING_KEY_ORDEN_CREADA,
                evento.getOrdenId(),
                evento.getUsuarioId());
    }
}