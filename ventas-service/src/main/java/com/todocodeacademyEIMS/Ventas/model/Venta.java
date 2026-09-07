package com.todocodeacademyEIMS.Ventas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;

    // Logical reference to the cart this sale was confirmed from (carritodb).
    // Kept as a plain id on purpose: that table lives in another service's database,
    // so it is never modelled as a JPA relation.
    private Long idCarrito;

    private LocalDateTime fechaVenta;

    // Items are saved and deleted through the sale (cascade + orphanRemoval),
    // so no separate VentaItem repository is needed.
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaItem> items;

    private double total;

}
