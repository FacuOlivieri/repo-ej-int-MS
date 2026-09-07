package com.todocodeacademyEIMS.Ventas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "VentaItem")
public class VentaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVentaLine;

    // Reference to a Product owned by the Producto microservice (productodb).
    // Kept as a plain id on purpose: it is resolved over HTTP, never as a JPA relation,
    // because that table lives in another service's database.
    private Long idProduct;

    // Descriptive product data frozen at sale time, part of the immutable sale snapshot.
    private String name;

    private String brand;

    private int quantity;

    // Price frozen at sale time, so the sale stays immutable if the product price
    // changes later.
    private double unitPrice;

    private double subtotal;

    @ManyToOne
    @JoinColumn(name = "idVenta")
    private Venta venta;

}
