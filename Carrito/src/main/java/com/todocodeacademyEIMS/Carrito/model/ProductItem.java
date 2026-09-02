package com.todocodeacademyEIMS.Carrito.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ProductItem")
public class ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProductLine;

    // Reference to a Product owned by the Producto microservice (productodb).
    // Kept as a plain id on purpose: it is resolved over HTTP, never as a JPA relation,
    // because that table lives in another service's database.
    private Long idProduct;

    private int quantity;

    // Price snapshot taken when the item was added, so the cart does not silently
    // change if the product price changes later.
    private double unitPrice;

    private double subtotal;

    @ManyToOne
    @JoinColumn(name = "idCarrito")
    private Carrito carrito;

}