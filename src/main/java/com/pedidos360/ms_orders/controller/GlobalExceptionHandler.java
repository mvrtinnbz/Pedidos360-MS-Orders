package com.pedidos360.ms_orders.controller;

import com.pedidos360.ms_orders.exception.OrdenException;
import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Respuestas de error en JSON uniformes para el frontend.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrdenException.class)
    public ResponseEntity<Map<String, Object>> handleOrden(OrdenException ex) {
        return error(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> handleRabbit(AmqpException ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo publicar el evento de la orden en RabbitMQ; la orden no fue registrada");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return error(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion no es un JSON valido");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String mensaje) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);

        return ResponseEntity.status(status).body(body);
    }
}
