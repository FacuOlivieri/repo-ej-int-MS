package com.todocodeacademyEIMS.Carrito.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Carrito")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCarrito;

    // Items are saved and deleted through the cart (cascade + orphanRemoval),
    // so no separate ProductItem repository is needed.
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductItem> productList;

    private double totalPrice;

}
