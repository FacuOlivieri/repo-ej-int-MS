package com.todocodeacademyEIMS.Carrito.repository;

import com.todocodeacademyEIMS.Carrito.dto.CarritoDTO;
import com.todocodeacademyEIMS.Carrito.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "producto-service")
public interface ProductoAPI {


    @GetMapping("/products/search")
    ProductDTO findProductByName(@RequestParam String productName);
}
