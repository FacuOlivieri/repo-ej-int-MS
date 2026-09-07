package com.todocodeacademyEIMS.Ventas.dto;

import lombok.*;

import java.util.List;

/**
 * Client-side copy used to deserialize the carrito-service response.
 * Duplicated on purpose: each microservice is autonomous and owns its own DTOs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDTO {

    private Long idCarrito;
    private List<ProductItemDTO> productList;
    private double totalPrice;
}
