package com.todocodeacademyEIMS.Carrito.service;

import com.todocodeacademyEIMS.Carrito.dto.CarritoDTO;

import java.util.List;

public interface ICarritoService {

    List<CarritoDTO> findAll();

    CarritoDTO findById(Long idCarrito);

    CarritoDTO save(CarritoDTO carritoDTO);

    CarritoDTO update(Long idCarrito, CarritoDTO carritoDTO);

    void deleteById(Long idCarrito);
}