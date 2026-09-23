package com.pedidos360.ms_orders.dto;

import java.util.ArrayList;
import java.util.List;

public class OrdenRequest {

    private String usuarioId;
    private String email;
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
