package com.pedidos360.ms_orders.service;

import com.pedidos360.ms_orders.client.ProductoClient;
import com.pedidos360.ms_orders.client.ProductoDto;
import com.pedidos360.ms_orders.dto.OrdenItemRequest;
import com.pedidos360.ms_orders.dto.OrdenRequest;
import com.pedidos360.ms_orders.entity.Orden;
import com.pedidos360.ms_orders.entity.OrdenItem;
import com.pedidos360.ms_orders.event.OrdenCreadaEvent;
import com.pedidos360.ms_orders.event.OrdenItemEvent;
import com.pedidos360.ms_orders.exception.OrdenException;
import com.pedidos360.ms_orders.messaging.OrdenEventPublisher;
import com.pedidos360.ms_orders.repository.OrdenRepository;
import com.pedidos360.ms_orders.security.UsuarioActual;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrdenService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final OrdenRepository repository;
    private final OrdenEventPublisher publisher;
    private final ProductoClient productoClient;

    // Permite desactivar la consulta a ms-productos (pruebas aisladas en Postman).
    @Value("${ordenes.validar-productos:true}")
    private boolean validarProductos;

    public OrdenService(OrdenRepository repository,
                        OrdenEventPublisher publisher,
                        ProductoClient productoClient) {
        this.repository = repository;
        this.publisher = publisher;
        this.productoClient = productoClient;
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

    // Valida los productos, guarda la orden en MySQL y publica el evento en RabbitMQ.
    // Si la publicacion falla, la transaccion hace rollback y la orden no queda registrada.
    @Transactional
    public Orden crearOrden(OrdenRequest request) {

        String usuarioId = esVacio(request.getUsuarioId())
                ? UsuarioActual.usuarioId()
                : request.getUsuarioId();

        String email = esVacio(request.getEmail())
                ? UsuarioActual.email()
                : request.getEmail();

        if (esVacio(usuarioId)) {
            throw new OrdenException(HttpStatus.BAD_REQUEST, "No se pudo determinar el usuario de la orden");
        }

        Orden orden = new Orden(usuarioId, email);

        for (OrdenItemRequest itemRequest : agruparPorProducto(request.getItems())) {
            orden.agregarItem(construirItem(itemRequest));
        }

        orden.recalcularTotal();

        Orden guardada = repository.save(orden);

        publisher.publicarOrdenCreada(construirEvento(guardada));

        return guardada;
    }

    // Si el mismo producto viene repetido, se suman las cantidades.
    private List<OrdenItemRequest> agruparPorProducto(List<OrdenItemRequest> items) {

        Map<Long, OrdenItemRequest> agrupados = new LinkedHashMap<>();

        for (OrdenItemRequest item : items) {
            agrupados.merge(item.getProductoId(), item, (a, b) -> {
                a.setCantidad(a.getCantidad() + b.getCantidad());
                return a;
            });
        }

        return List.copyOf(agrupados.values());
    }

    private OrdenItem construirItem(OrdenItemRequest itemRequest) {

        if (!validarProductos) {

            if (itemRequest.getPrecioUnitario() == null || esVacio(itemRequest.getNombreProducto())) {
                throw new OrdenException(HttpStatus.BAD_REQUEST,
                        "El producto " + itemRequest.getProductoId() + " debe incluir nombre y precio");
            }

            return new OrdenItem(
                    itemRequest.getProductoId(),
                    itemRequest.getNombreProducto(),
                    itemRequest.getPrecioUnitario(),
                    itemRequest.getCantidad()
            );
        }

        // El catalogo es la fuente de verdad del precio y del stock.
        ProductoDto producto = productoClient.obtener(itemRequest.getProductoId());

        int stock = producto.getStock() != null ? producto.getStock() : 0;

        if (stock < itemRequest.getCantidad()) {
            throw new OrdenException(HttpStatus.CONFLICT,
                    "Stock insuficiente para '" + producto.getNombre()
                            + "' (disponible: " + stock + ", solicitado: " + itemRequest.getCantidad() + ")");
        }

        return new OrdenItem(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                itemRequest.getCantidad()
        );
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

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
