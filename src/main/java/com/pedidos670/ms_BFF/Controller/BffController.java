package com.pedidos670.ms_BFF.Controller;

import com.pedidos670.ms_BFF.clients.CatalogoClient;
import com.pedidos670.ms_BFF.clients.PedidosClient;
import com.pedidos670.ms_BFF.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/bff")
@RequiredArgsConstructor
public class BffController {

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final CatalogoClient catalogoClient;
    private final PedidosClient pedidosClient;

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

    @PostMapping("/pedidos")
    public ResponseEntity<?> crearPedido(
            @RequestBody CrearPedidoDesdeBffRequest request
    ) {
        return procesarCreacionPedido(
                request.getClienteId(),
                request.getItems()
        );
    }

    @PostMapping("/pedidos/invitado")
    public ResponseEntity<?> crearPedidoInvitado(
            @RequestBody CrearPedidoDesdeBffRequest request
    ) {

        String email =
                request.getClienteId() == null
                        ? ""
                        : request.getClienteId()
                        .trim()
                        .toLowerCase();

        if (!EMAIL.matcher(email).matches()) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "Debes indicar un correo válido."
                    );
        }

        return procesarCreacionPedido(
                email,
                request.getItems()
        );
    }

    @GetMapping("/pedidos/seguimiento/{codigo}")
    public ResponseEntity<?> seguimientoPorCodigo(
            @PathVariable String codigo
    ) {

        try {

            return ResponseEntity.ok(
                    pedidosClient.obtenerPorCodigoSeguimiento(
                            codigo.trim().toUpperCase()
                    )
            );

        } catch (HttpClientErrorException.NotFound e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            "Pedido no encontrado."
                    );
        }
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<OrderResponseDTO>> listarPedidos() {
        return ResponseEntity.ok(
                pedidosClient.obtenerTodos()
        );
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<OrderResponseDTO> obtenerPedido(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pedidosClient.obtenerPorId(id)
        );
    }

    @PatchMapping("/pedidos/{id}/status")
    public ResponseEntity<OrderResponseDTO> cambiarEstadoPedido(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(
                pedidosClient.cambiarEstado(
                        id,
                        status
                )
        );
    }

    private ResponseEntity<?> procesarCreacionPedido(
            String clienteId,
            List<ItemSolicitadoDTO> itemsSolicitados
    ) {

        if (
                itemsSolicitados == null
                        || itemsSolicitados.isEmpty()
        ) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "El pedido debe tener al menos un producto."
                    );
        }

        double total = 0;

        List<OrderItemRequestDTO> items =
                new ArrayList<>();

        for (ItemSolicitadoDTO itemReq : itemsSolicitados) {

            RespuestaProductoDTO producto =
                    catalogoClient.obtenerProducto(
                            itemReq.getProductoId()
                    );

            if (
                    producto.getStock()
                            < itemReq.getCantidad()
            ) {
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

            total +=
                    producto.getPrecio().doubleValue()
                            * itemReq.getCantidad();
        }

        OrderRequestDTO orderRequest =
                new OrderRequestDTO();

        orderRequest.setClienteId(
                clienteId
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
}