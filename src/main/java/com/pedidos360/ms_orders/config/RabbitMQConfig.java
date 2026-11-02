package com.pedidos360.ms_orders.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
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

    // Resultado de los pagos (publica ms-pago: pago.aprobado / pago.rechazado)
    public static final String QUEUE_ORDENES_PAGOS =
            "ordenes.pagos.queue";

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
    public Queue ordenesPagosQueue() {
        return QueueBuilder
                .durable(QUEUE_ORDENES_PAGOS)
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

    // "pago.*" -> recibe pago.aprobado y pago.rechazado
    @Bean
    public Binding bindingOrdenesPagos() {
        return BindingBuilder
                .bind(ordenesPagosQueue())
                .to(pedidos360Exchange())
                .with("pago.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter("com.pedidos360");
        // El evento viene de otro microservicio (otra clase Java):
        // se convierte al tipo del parametro del @RabbitListener.
        converter.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}