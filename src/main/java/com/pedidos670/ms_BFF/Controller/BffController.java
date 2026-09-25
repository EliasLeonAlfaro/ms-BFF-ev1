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

    // =====================================================
    // CATÁLOGO
    // =====================================================

    @GetMapping("/productos")
    public ResponseEntity<List<RespuestaProductoDTO>> listarProductos() {
        return ResponseEntity.ok(
                catalogoClient.listarProductos()
        );
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<RespuestaProductoDTO> obtenerProducto(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                catalogoClient.obtenerProducto(id)
        );
    }
    @PostMapping("/productos")
    @PreAuthorize("hasRole('OPERADOR','ADMIN')")
    public ResponseEntity<?> crearProducto(
            @RequestBody GuardarProductoDTO request
    ) {
        try {

            RespuestaProductoDTO productoCreado =
                    catalogoClient.crearProducto(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(productoCreado);

        } catch (org.springframework.web.client.HttpClientErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (org.springframework.web.client.HttpServerErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error al crear producto: "
                                    + e.getMessage()
                    );
        }
    }
    @PutMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @RequestBody GuardarProductoDTO request
    ) {
        try {

            RespuestaProductoDTO actualizado =
                    catalogoClient.actualizarProducto(
                            id,
                            request
                    );

            return ResponseEntity.ok(actualizado);

        } catch (org.springframework.web.client.HttpClientErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (org.springframework.web.client.HttpServerErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error al actualizar producto: "
                                    + e.getMessage()
                    );
        }
    }
    @DeleteMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarProducto(
            @PathVariable Long id
    ) {
        try {

            catalogoClient.eliminarProducto(id);

            return ResponseEntity
                    .noContent()
                    .build();

        } catch (org.springframework.web.client.HttpClientErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (org.springframework.web.client.HttpServerErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error al eliminar producto: "
                                    + e.getMessage()
                    );
        }
    }

    // =====================================================
    // PEDIDOS
    // =====================================================

    @PostMapping("/pedidos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN', 'OPERADOR')")
    public ResponseEntity<?> crearPedido(
            @RequestBody CrearPedidoDesdeBffRequest request
    ) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("El pedido debe contener al menos un producto");
        }

        if (request.getItems().size() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body("Solo se puede comprar un producto por pedido");
        }

        double total = 0;
        List<OrderItemRequestDTO> items = new ArrayList<>();

        for (ItemSolicitadoDTO itemReq : request.getItems()) {

            if (itemReq.getCantidad() == null || itemReq.getCantidad() <= 0) {
                return ResponseEntity
                        .badRequest()
                        .body("La cantidad debe ser mayor a cero");
            }

            RespuestaProductoDTO producto =
                    catalogoClient.obtenerProducto(
                            itemReq.getProductoId()
                    );

            if (producto.getStock() < itemReq.getCantidad()) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                "Stock insuficiente para: "
                                        + producto.getNombre()
                        );
            }

            OrderItemRequestDTO item =
                    new OrderItemRequestDTO();

            item.setProductoId(
                    producto.getId()
            );

            item.setCantidad(
                    itemReq.getCantidad()
            );

            item.setPrecioUnitario(
                    producto.getPrecio().doubleValue()
            );

            items.add(item);

            total += producto.getPrecio().doubleValue()
                    * itemReq.getCantidad();
        }

        OrderRequestDTO orderRequest =
                new OrderRequestDTO();

        orderRequest.setClienteId(
                request.getClienteId()
        );

        orderRequest.setEstado(
                "CREADO"
        );

        orderRequest.setTotal(
                total
        );

        orderRequest.setItems(
                items
        );

        OrderResponseDTO pedidoCreado =
                pedidosClient.crearPedido(
                        orderRequest
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pedidoCreado);
    }

    // =====================================================
    // CONSULTA DE PEDIDOS
    // =====================================================

    // OPERADOR y ADMIN pueden ver todos
    @GetMapping("/pedidos")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> listarPedidos() {

        return ResponseEntity.ok(
                pedidosClient.obtenerTodos()
        );
    }

    // CLIENTE puede consultar sus pedidos por clienteId
    @GetMapping("/pedidos/client/{clienteId}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<OrderResponseDTO>> obtenerPedidosPorCliente(
            @PathVariable String clienteId
    ) {

        return ResponseEntity.ok(
                pedidosClient.obtenerPorCliente(clienteId)
        );
    }

    // CLIENTE, OPERADOR y ADMIN pueden consultar por ID
    @GetMapping("/pedidos/{id}")
    @PreAuthorize(
            "hasAnyRole('CLIENTE', 'OPERADOR', 'ADMIN')"
    )
    public ResponseEntity<OrderResponseDTO> obtenerPedido(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                pedidosClient.obtenerPorId(id)
        );
    }

    // =====================================================
    // CAMBIO DE ESTADO
    // =====================================================

    @PatchMapping("/pedidos/{id}/status")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<?> cambiarEstadoPedido(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        try {

            OrderResponseDTO pedidoActualizado =
                    pedidosClient.cambiarEstado(
                            id,
                            status
                    );

            return ResponseEntity.ok(
                    pedidoActualizado
            );

        } catch (org.springframework.web.client.HttpClientErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error al cambiar el estado del pedido: "
                                    + e.getMessage()
                    );
        }
    }

    // =====================================================
    // CANCELACIÓN
    // =====================================================

    @PatchMapping("/pedidos/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> cancelarPedido(
            @PathVariable Long id
    ) {
        try {

            OrderResponseDTO pedidoCancelado =
                    pedidosClient.cancelarPedido(id);

            return ResponseEntity.ok(
                    pedidoCancelado
            );

        } catch (org.springframework.web.client.HttpClientErrorException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cancelar el pedido: " + e.getMessage());
        }
    }
}