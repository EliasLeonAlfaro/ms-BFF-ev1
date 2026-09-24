package com.pedidos670.ms_BFF.clients;

import org.springframework.web.bind.annotation.*;

import com.pedidos670.ms_BFF.dtos.OrderRequestDTO;
import com.pedidos670.ms_BFF.dtos.OrderResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class PedidosClient {

    private final RestClient restClient;

    public PedidosClient(@Value("${pedidos.url}") String pedidosUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(pedidosUrl)
                .build();
    }

    public OrderResponseDTO crearPedido(OrderRequestDTO request) {
        return restClient.post()
                .uri("/api/pedidos")
                .body(request)
                .retrieve()
                .body(OrderResponseDTO.class);
    }

    public List<OrderResponseDTO> obtenerTodos() {
        return restClient.get()
                .uri("/api/pedidos")
                .retrieve()
                .body(List.class);
    }

    public OrderResponseDTO obtenerPorId(Long id) {
        return restClient.get()
                .uri("/api/pedidos/{id}", id)
                .retrieve()
                .body(OrderResponseDTO.class);
    }

    public OrderResponseDTO cambiarEstado(Long id, String status) {
        return restClient.patch()
                .uri("/api/pedidos/{id}/status?status={status}", id, status)
                .retrieve()
                .body(OrderResponseDTO.class);
    }
}