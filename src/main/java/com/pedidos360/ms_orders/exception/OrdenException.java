package com.pedidos360.ms_orders.exception;

import org.springframework.http.HttpStatus;

// Error de negocio al crear una orden (producto inexistente, sin stock, etc.).
public class OrdenException extends RuntimeException {

    private final HttpStatus status;

    public OrdenException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
