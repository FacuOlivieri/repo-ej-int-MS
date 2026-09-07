package com.todocodeacademyEIMS.Ventas.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {

    private Long idVenta;
    private Long idCarrito;
    private LocalDateTime fechaVenta;
    private List<VentaItemDTO> items;
    private double total;
}
