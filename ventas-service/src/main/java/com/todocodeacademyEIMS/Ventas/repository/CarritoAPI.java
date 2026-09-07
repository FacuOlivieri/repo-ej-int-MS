package com.todocodeacademyEIMS.Ventas.repository;

import com.todocodeacademyEIMS.Ventas.dto.CarritoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "carrito-service")
public interface CarritoAPI {

    @GetMapping("/carritos/find/{idCarrito}")
    CarritoDTO findCarritoById(@PathVariable Long idCarrito);
}
