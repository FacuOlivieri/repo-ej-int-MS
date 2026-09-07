package com.todocodeacademyEIMS.Ventas.dto;

import lombok.*;

/**
 * Body of the endpoint that confirms a sale.
 * The cart is identified by id and resolved against carrito-service via Feign;
 * its lines and amounts are copied into the sale as a snapshot.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVentaRequestDTO {

    private Long idCarrito;
}
