package com.todocodeacademyEIMS.Carrito.dto;

import lombok.*;

/**
 * Body del endpoint que agrega un producto al carrito.
 * El producto se identifica por nombre (se resuelve contra el microservicio Producto
 * vía Feign) y se indica cuántas unidades ingresar.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddProductRequestDTO {

    private String productName;
    private int quantity;
}
