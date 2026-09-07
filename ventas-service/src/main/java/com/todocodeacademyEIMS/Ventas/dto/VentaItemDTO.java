package com.todocodeacademyEIMS.Ventas.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaItemDTO {

    private Long idVentaLine;
    private Long idProduct;
    private String name;
    private String brand;
    private int quantity;
    private double unitPrice;
    private double subtotal;
}
