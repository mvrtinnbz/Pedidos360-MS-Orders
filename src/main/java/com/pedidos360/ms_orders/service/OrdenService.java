package com.pedidos360.ms_orders.service;

import com.pedidos360.ms_orders.dto.OrdenItemRequest;
import com.pedidos360.ms_orders.dto.OrdenRequest;
import com.pedidos360.ms_orders.entity.Orden;
import com.pedidos360.ms_orders.entity.OrdenItem;
import com.pedidos360.ms_orders.event.OrdenCreadaEvent;
import com.pedidos360.ms_orders.event.OrdenItemEvent;
import com.pedidos360.ms_orders.messaging.OrdenEventPublisher;
import com.pedidos360.ms_orders.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrdenService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final OrdenRepository repository;
    private final OrdenEventPublisher publisher;

    public OrdenService(OrdenRepository repository, OrdenEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public List<Orden> findAll() {
        return repository.findAll();
    }

    public Orden findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Orden> findByUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    // Guarda la orden en MySQL y recien despues publica el evento en RabbitMQ.
    @Transactional
    public Orden crearOrden(OrdenRequest request) {

        Orden orden = new Orden(request.getUsuarioId(), request.getEmail());

        for (OrdenItemRequest itemRequest : request.getItems()) {

            OrdenItem item = new OrdenItem(
                    itemRequest.getProductoId(),
                    itemRequest.getNombreProducto(),
                    itemRequest.getPrecioUnitario(),
                    itemRequest.getCantidad()
            );

            orden.agregarItem(item);
        }

        orden.recalcularTotal();

        Orden guardada = repository.save(orden);

        publisher.publicarOrdenCreada(construirEvento(guardada));

        return guardada;
    }

    private OrdenCreadaEvent construirEvento(Orden orden) {

        OrdenCreadaEvent evento = new OrdenCreadaEvent();

        evento.setOrdenId(orden.getId());
        evento.setUsuarioId(orden.getUsuarioId());
        evento.setEmail(orden.getEmail());
        evento.setTotal(orden.getTotal());
        evento.setFechaCreacion(orden.getFechaCreacion().format(FORMATO_FECHA));

        List<OrdenItemEvent> items = orden.getItems().stream()
                .map(item -> new OrdenItemEvent(
                        item.getProductoId(),
                        item.getNombreProducto(),
                        item.getPrecioUnitario(),
                        item.getCantidad()
                ))
                .toList();

        evento.setItems(items);

        return evento;
    }
}