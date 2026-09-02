package com.todocodeacademyEIMS.Producto.controller;

import com.todocodeacademyEIMS.Producto.dto.ProductDTO;
import com.todocodeacademyEIMS.Producto.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping("/find/all")
    public ResponseEntity<List<ProductDTO>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/find/{idProduct}")
    public ResponseEntity<ProductDTO> findById(@PathVariable Long idProduct) {
        return ResponseEntity.ok(productService.findById(idProduct));
    }

    @GetMapping("/search")
    public ResponseEntity<ProductDTO> findByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.findByName(name));
    }

    @PostMapping("/save")
    public ResponseEntity<ProductDTO> save(@RequestBody ProductDTO productDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(productDTO));
    }

    @PutMapping("/update/{idProduct}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long idProduct,
                                             @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.update(idProduct, productDTO));
    }

    @DeleteMapping("/delete/{idProduct}")
    public ResponseEntity<Void> deleteById(@PathVariable Long idProduct) {
        productService.deleteById(idProduct);
        return ResponseEntity.noContent().build();
    }
}