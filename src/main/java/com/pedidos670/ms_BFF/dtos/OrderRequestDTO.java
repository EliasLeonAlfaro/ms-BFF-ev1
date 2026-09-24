package com.pedidos670.ms_BFF.dtos;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private String clienteId;
    private String estado; // "CREADO" al iniciar
    private Double total;
    private List<OrderItemRequestDTO> items;
}
