package com.pedidos360.ms_orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class OrdenRequest {

    // Si no vienen, se toman del JWT del usuario autenticado.
    private String usuarioId;
    private String email;

    @NotEmpty(message = "la orden debe tener al menos un item")
    @Valid
    private List<OrdenItemRequest> items = new ArrayList<>();

    public OrdenRequest() {
    }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<OrdenItemRequest> getItems() { return items; }
    public void setItems(List<OrdenItemRequest> items) { this.items = items; }
}
