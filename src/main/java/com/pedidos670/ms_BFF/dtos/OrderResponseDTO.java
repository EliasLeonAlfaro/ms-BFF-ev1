package com.pedidos670.ms_BFF.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private String clienteId;
    private String estado;
    private LocalDateTime fechaCreacion;
    private Double total;
    private List<OrderItemRequestDTO> items;
}
