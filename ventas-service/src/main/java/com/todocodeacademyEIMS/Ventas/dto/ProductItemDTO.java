package com.todocodeacademyEIMS.Ventas.dto;

import lombok.*;

/**
 * Client-side copy used to deserialize the carrito-service response.
 * Duplicated on purpose: each microservice is autonomous and owns its own DTOs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductItemDTO {

    private Long idProductLine;
    private ProductDTO product;
    private int quantity;
    private double subtotal;
}
