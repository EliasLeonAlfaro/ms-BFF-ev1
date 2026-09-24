package com.pedidos670.ms_BFF.dtos;

import lombok.Data;
import java.util.List;

@Data
public class CrearPedidoDesdeBffRequest {
    private String clienteId;
    private List<ItemSolicitadoDTO> items;
}
