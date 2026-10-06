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

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "pedidos360.exchange";
    public static final String ROUTING_KEY_ORDEN_CREADA = "orden.creada";

    public static final String QUEUE_PRODUCTOS_STOCK =
            "productos.stock.queue";

    public static final String QUEUE_NOTIFICACIONES_EMAIL =
            "notificaciones.email.queue";

    public static final String QUEUE_FACTURACION =
            "facturacion.queue";

    @Bean
    public TopicExchange pedidos360Exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue productosStockQueue() {
        return QueueBuilder
                .durable(QUEUE_PRODUCTOS_STOCK)
                .build();
    }

    @Bean
    public Queue notificacionesEmailQueue() {
        return QueueBuilder
                .durable(QUEUE_NOTIFICACIONES_EMAIL)
                .build();
    }

    @Bean
    public Queue facturacionQueue() {
        return QueueBuilder
                .durable(QUEUE_FACTURACION)
                .build();
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

    @Bean
    public Binding bindingFacturacion() {
        return BindingBuilder
                .bind(facturacionQueue())
                .to(pedidos360Exchange())
                .with(ROUTING_KEY_ORDEN_CREADA);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter("com.pedidos360");
    }
}