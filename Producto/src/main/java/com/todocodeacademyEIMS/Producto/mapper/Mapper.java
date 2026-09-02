package com.todocodeacademyEIMS.Producto.mapper;

import com.todocodeacademyEIMS.Producto.dto.ProductDTO;
import com.todocodeacademyEIMS.Producto.model.Product;

public class Mapper {

    public static ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .idProduct(product.getIdProduct())
                .name(product.getName())
                .brand(product.getBrand())
                .unitPrice(product.getUnitPrice())
                .build();
    }

    public static Product mapFromDTO(ProductDTO productDTO) {
        return Product.builder()
                .idProduct(productDTO.getIdProduct())
                .name(productDTO.getName())
                .brand(productDTO.getBrand())
                .unitPrice(productDTO.getUnitPrice())
                .build();
    }
}
