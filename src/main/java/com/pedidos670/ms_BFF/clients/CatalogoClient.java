package com.pedidos670.ms_BFF.clients;

import com.pedidos670.ms_BFF.dtos.GuardarProductoDTO;
import com.pedidos670.ms_BFF.dtos.RespuestaProductoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class CatalogoClient {

    private final RestClient restClient;

    public CatalogoClient(@Value("${catalogo.url}") String catalogoUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(catalogoUrl)
                .build();
    }

    public List<RespuestaProductoDTO> listarProductos() {
        return restClient.get()
                .uri("/api/catalogo/productos")
                .retrieve()
                .body(List.class);
    }

    public RespuestaProductoDTO obtenerProducto(Long id) {
        return restClient.get()
                .uri("/api/catalogo/productos/{id}", id)
                .retrieve()
                .body(RespuestaProductoDTO.class);
    }

    public RespuestaProductoDTO crearProducto(
            GuardarProductoDTO request
    ) {
        return restClient.post()
                .uri("/api/catalogo/productos")
                .body(request)
                .retrieve()
                .body(RespuestaProductoDTO.class);
    }

    public RespuestaProductoDTO actualizarProducto(Long id, GuardarProductoDTO dto) {
        return restClient.put()
                .uri("/api/catalogo/productos/{id}", id)
                .body(dto)
                .retrieve()
                .body(RespuestaProductoDTO.class);
    }

    public RespuestaProductoDTO descontarStock(Long id, Integer cantidad) {
        return restClient.patch()
                .uri("/api/catalogo/productos/{id}/stock?cantidad={cantidad}", id, cantidad)
                .retrieve()
                .body(RespuestaProductoDTO.class);
    }

    public void eliminarProducto(Long id) {
        restClient.delete()
                .uri("/api/catalogo/productos/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}