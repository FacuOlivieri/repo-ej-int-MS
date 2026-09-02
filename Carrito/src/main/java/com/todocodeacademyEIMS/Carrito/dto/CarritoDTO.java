package com.todocodeacademyEIMS.Carrito.dto;

import lombok.*;

import java.util.List;

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