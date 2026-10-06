package com.pedidos360.ms_orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OrdenItemRequest {

    @NotNull(message = "productoId es obligatorio")
    private Long productoId;

    // Informativos: si la validacion con ms-productos esta activa,
    // el nombre y el precio se toman del catalogo.
    private String nombreProducto;
    private BigDecimal precioUnitario;

    @NotNull(message = "cantidad es obligatoria")
    @Min(value = 1, message = "la cantidad debe ser al menos 1")
    private Integer cantidad;

    public OrdenItemRequest() {
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
