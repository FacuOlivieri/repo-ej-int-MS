package com.todocodeacademyEIMS.Carrito.dto;

import lombok.*;

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
