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
public class ProductDTO {

    private Long idProduct;
    private String name;
    private String brand;
    private double unitPrice;
}
