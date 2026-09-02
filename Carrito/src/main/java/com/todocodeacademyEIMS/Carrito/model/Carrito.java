package com.todocodeacademyEIMS.Carrito.model;


import com.todocodeacademyEIMS.Carrito.dto.ProductItemDTO;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {
    
    private Long idCarrito;
    private List<ProductItemDTO> productList;
    private double totalPrice;

}
