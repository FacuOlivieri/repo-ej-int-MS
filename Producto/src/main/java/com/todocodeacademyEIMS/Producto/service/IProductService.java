package com.todocodeacademyEIMS.Producto.service;

import com.todocodeacademyEIMS.Producto.dto.ProductDTO;

import java.util.List;

public interface IProductService {

    List<ProductDTO> findAll();

    ProductDTO findById(Long idProduct);

    ProductDTO findByName(String name);

    ProductDTO save(ProductDTO productDTO);

    ProductDTO update(Long idProduct, ProductDTO productDTO);

    void deleteById(Long idProduct);
}