package com.pedidos670.ms_BFF.dtos;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private Long productoId;
    private Integer cantidad;
    private Double precioUnitario;
}
