package com.todocodeacademyEIMS.Producto.dto;


import lombok.*;


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
