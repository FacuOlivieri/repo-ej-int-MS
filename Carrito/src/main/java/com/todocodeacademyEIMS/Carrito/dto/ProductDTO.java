package com.todocodeacademyEIMS.Carrito.dto;

import lombok.*;

@Builder
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long idProduct;
    private String name;
    private String brand;
    private double unitPrice;
}
