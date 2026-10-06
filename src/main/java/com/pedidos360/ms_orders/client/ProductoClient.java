package com.pedidos360.ms_orders.client;

import com.pedidos360.ms_orders.exception.OrdenException;
import com.pedidos360.ms_orders.security.UsuarioActual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Consulta sincrona a ms-productos para validar existencia, precio y stock
// antes de registrar la orden. Reenvia el JWT del usuario.
@Component
public class ProductoClient {

    private static final Logger log = LoggerFactory.getLogger(ProductoClient.class);

    private final RestClient restClient;

    public ProductoClient(@Value("${productos.api.url}") String productosUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(10000);

        this.restClient = RestClient.builder()
                .baseUrl(productosUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public ProductoDto obtener(Long productoId) {

        try {

            return restClient.get()
                    .uri("/api/productos/{id}", productoId)
                    .headers(headers -> {
                        String token = UsuarioActual.token();
                        if (token != null) {
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                        }
                    })
                    .retrieve()
                    .body(ProductoDto.class);

        } catch (HttpClientErrorException.NotFound e) {

            throw new OrdenException(HttpStatus.BAD_REQUEST,
                    "El producto " + productoId + " no existe");

        } catch (RestClientException e) {

            log.error("No se pudo consultar ms-productos (producto {}): {}", productoId, e.getMessage());

            throw new OrdenException(HttpStatus.BAD_GATEWAY,
                    "No se pudo validar el producto " + productoId + " con ms-productos");
        }
    }
}
