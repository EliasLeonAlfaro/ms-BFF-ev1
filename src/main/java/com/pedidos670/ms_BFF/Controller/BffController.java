package com.pedidos670.ms_BFF.Controller;

import com.pedidos670.ms_BFF.clients.CatalogoClient;
import com.pedidos670.ms_BFF.clients.PedidosClient;
import com.pedidos670.ms_BFF.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bff")
@RequiredArgsConstructor
public class BffController {

    private final CatalogoClient catalogoClient;
    private final PedidosClient pedidosClient;

    // ===== CATÁLOGO =====

    @GetMapping("/productos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<List<RespuestaProductoDTO>> listarProductos() {
        return ResponseEntity.ok(catalogoClient.listarProductos());
    }

    @GetMapping("/productos/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<RespuestaProductoDTO> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoClient.obtenerProducto(id));
    }

    // ===== PEDIDOS =====

    @PostMapping("/pedidos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<?> crearPedido(@RequestBody CrearPedidoDesdeBffRequest request) {
        double total = 0;
        List<OrderItemRequestDTO> items = new ArrayList<>();

        for (ItemSolicitadoDTO itemReq : request.getItems()) {
            RespuestaProductoDTO producto = catalogoClient.obtenerProducto(itemReq.getProductoId());

            if (producto.getStock() < itemReq.getCantidad()) {
                return ResponseEntity.badRequest()
                        .body("Stock insuficiente para: " + producto.getNombre());
            }

            OrderItemRequestDTO item = new OrderItemRequestDTO();
            item.setProductoId(producto.getId());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(producto.getPrecio().doubleValue());
            items.add(item);

            total += producto.getPrecio().doubleValue() * itemReq.getCantidad();
        }

        OrderRequestDTO orderRequest = new OrderRequestDTO();
        orderRequest.setClienteId(request.getClienteId());
        orderRequest.setEstado("CREADO");
        orderRequest.setTotal(total);
        orderRequest.setItems(items);

        OrderResponseDTO pedidoCreado = pedidosClient.crearPedido(orderRequest);

        for (ItemSolicitadoDTO itemReq : request.getItems()) {
            catalogoClient.descontarStock(itemReq.getProductoId(), itemReq.getCantidad());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCreado);
    }

    @GetMapping("/pedidos")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> listarPedidos() {
        return ResponseEntity.ok(pedidosClient.obtenerTodos());
    }

    @GetMapping("/pedidos/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> obtenerPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidosClient.obtenerPorId(id));
    }

    @PatchMapping("/pedidos/{id}/status")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> cambiarEstadoPedido(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(pedidosClient.cambiarEstado(id, status));
    }
}