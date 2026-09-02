package com.todocodeacademyEIMS.Producto.service;

import com.todocodeacademyEIMS.Producto.dto.ProductDTO;
import com.todocodeacademyEIMS.Producto.exception.ProductNotFoundException;
import com.todocodeacademyEIMS.Producto.mapper.Mapper;
import com.todocodeacademyEIMS.Producto.model.Product;
import com.todocodeacademyEIMS.Producto.repository.IProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService implements IProductService {

    @Autowired
    private IProductRepository productRepository;

    @Override
    public List<ProductDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(Mapper::mapToDTO)
                .toList();
    }

    @Override
    public ProductDTO findById(Long idProduct) {
        Product product = productRepository.findById(idProduct)
                .orElseThrow(() -> new ProductNotFoundException(idProduct));
        return Mapper.mapToDTO(product);
    }

    @Override
    public ProductDTO findByName(String name) {
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ProductNotFoundException(name));
        return Mapper.mapToDTO(product);
    }

    @Override
    public ProductDTO save(ProductDTO productDTO) {
        Product product = Mapper.mapFromDTO(productDTO);
        product.setIdProduct(null);
        return Mapper.mapToDTO(productRepository.save(product));
    }

    @Override
    public ProductDTO update(Long idProduct, ProductDTO productDTO) {
        Product product = productRepository.findById(idProduct).orElseThrow(() -> new ProductNotFoundException(idProduct));

        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setUnitPrice(productDTO.getUnitPrice());

        return Mapper.mapToDTO(productRepository.save(product));
    }

    @Override
    public void deleteById(Long idProduct) {
        if (!productRepository.existsById(idProduct)) {
            throw new ProductNotFoundException(idProduct);
        }
        productRepository.deleteById(idProduct);
    }
}