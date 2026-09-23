package com.pedidos360.ms_orders.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Topologia de mensajeria de Pedidos360.
// ms-orders es el PRODUCTOR: publica el evento "orden.creada" en el exchange
// y ese mismo evento lo reciben en paralelo ms-productos (descuento de stock)
// y ms-notificaciones (envio de correo).
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "pedidos360.exchange";
    public static final String ROUTING_KEY_ORDEN_CREADA = "orden.creada";

    public static final String QUEUE_PRODUCTOS_STOCK = "productos.stock.queue";
    public static final String QUEUE_NOTIFICACIONES_EMAIL = "notificaciones.email.queue";

    @Bean
    public TopicExchange pedidos360Exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // Las colas se declaran aqui (durables) para que los mensajes no se pierdan
    // si los consumidores todavia no estan levantados.
    @Bean
    public Queue productosStockQueue() {
        return QueueBuilder.durable(QUEUE_PRODUCTOS_STOCK).build();
    }

    @Bean
    public Queue notificacionesEmailQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICACIONES_EMAIL).build();
    }

    @Bean
    public Binding bindingProductosStock() {
        return BindingBuilder
                .bind(productosStockQueue())
                .to(pedidos360Exchange())
                .with(ROUTING_KEY_ORDEN_CREADA);
    }

    @Bean
    public Binding bindingNotificacionesEmail() {
        return BindingBuilder
                .bind(notificacionesEmailQueue())
                .to(pedidos360Exchange())
                .with(ROUTING_KEY_ORDEN_CREADA);
    }

    // Los mensajes viajan como JSON (no como serializacion Java),
    // asi cada microservicio puede tener su propia clase de evento.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter("com.pedidos360");
    }
}