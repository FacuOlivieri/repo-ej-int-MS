package com.todocodeacademyEIMS.Carrito.mapper;

import com.todocodeacademyEIMS.Carrito.dto.CarritoDTO;
import com.todocodeacademyEIMS.Carrito.dto.ProductDTO;
import com.todocodeacademyEIMS.Carrito.dto.ProductItemDTO;
import com.todocodeacademyEIMS.Carrito.model.Carrito;
import com.todocodeacademyEIMS.Carrito.model.ProductItem;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static CarritoDTO mapToDTO(Carrito carrito) {
        List<ProductItemDTO> productList = carrito.getProductList() == null
                ? List.of()
                : carrito.getProductList().stream().map(Mapper::mapToDTO).toList();

        return CarritoDTO.builder()
                .idCarrito(carrito.getIdCarrito())
                .productList(productList)
                .totalPrice(carrito.getTotalPrice())
                .build();
    }

    public static ProductItemDTO mapToDTO(ProductItem productItem) {
        // Only local data is available here: the product id and the stored price snapshot.
        // FEIGN SEAM: name and brand stay null until the Producto microservice is queried
        // by idProduct and its response is used to complete this ProductDTO.
        ProductDTO product = ProductDTO.builder()
                .idProduct(productItem.getIdProduct())
                .unitPrice(productItem.getUnitPrice())
                .build();

        return ProductItemDTO.builder()
                .idProductLine(productItem.getIdProductLine())
                .product(product)
                .quantity(productItem.getQuantity())
                .subtotal(productItem.getSubtotal())
                .build();
    }

    public static Carrito mapFromDTO(CarritoDTO carritoDTO) {
        Carrito carrito = Carrito.builder()
                .idCarrito(carritoDTO.getIdCarrito())
                .totalPrice(carritoDTO.getTotalPrice())
                .productList(new ArrayList<>())
                .build();

        if (carritoDTO.getProductList() != null) {
            for (ProductItemDTO productItemDTO : carritoDTO.getProductList()) {
                ProductItem productItem = mapFromDTO(productItemDTO);
                // Back-reference: without it the @ManyToOne stays null
                // and the idCarrito foreign key is never persisted.
                productItem.setCarrito(carrito);
                carrito.getProductList().add(productItem);
            }
        }

        return carrito;
    }

    public static ProductItem mapFromDTO(ProductItemDTO productItemDTO) {
        ProductDTO product = productItemDTO.getProduct();

        return ProductItem.builder()
                .idProductLine(productItemDTO.getIdProductLine())
                .idProduct(product == null ? null : product.getIdProduct())
                .quantity(productItemDTO.getQuantity())
                .unitPrice(product == null ? 0d : product.getUnitPrice())
                .build();
    }
}