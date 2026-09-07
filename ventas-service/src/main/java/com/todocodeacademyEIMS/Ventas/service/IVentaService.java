package com.todocodeacademyEIMS.Ventas.service;

import com.todocodeacademyEIMS.Ventas.dto.CreateVentaRequestDTO;
import com.todocodeacademyEIMS.Ventas.dto.VentaDTO;

import java.util.List;

public interface IVentaService {

    List<VentaDTO> findAll();

    VentaDTO findById(Long idVenta);

    VentaDTO confirmarVenta(CreateVentaRequestDTO request);

    void deleteById(Long idVenta);
}
