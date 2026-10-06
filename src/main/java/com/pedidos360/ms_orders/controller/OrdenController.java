package com.pedidos360.ms_orders.controller;

import com.pedidos360.ms_orders.dto.OrdenRequest;
import com.pedidos360.ms_orders.entity.Orden;
import com.pedidos360.ms_orders.security.UsuarioActual;
import com.pedidos360.ms_orders.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controladores REST para la gestión de órdenes - Pedidos360
@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService service;

    public OrdenController(OrdenService service) {
        this.service = service;
    }

    @GetMapping
    public List<Orden> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orden> getById(@PathVariable Long id) {

        Orden orden = service.findById(id);

        if (orden == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(orden);
    }

    // Ordenes del usuario autenticado (segun su JWT).
    @GetMapping("/mis-ordenes")
    public List<Orden> getMisOrdenes() {
        return service.findByUsuario(UsuarioActual.usuarioId());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Orden> getByUsuario(@PathVariable String usuarioId) {
        return service.findByUsuario(usuarioId);
    }

    @PostMapping
    public ResponseEntity<Orden> crearOrden(@Valid @RequestBody OrdenRequest request) {

        Orden creada = service.crearOrden(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }
}