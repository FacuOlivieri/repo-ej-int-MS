package com.todocodeacademyEIMS.Ventas.service;

import com.todocodeacademyEIMS.Ventas.dto.CarritoDTO;
import com.todocodeacademyEIMS.Ventas.dto.CreateVentaRequestDTO;
import com.todocodeacademyEIMS.Ventas.dto.VentaDTO;
import com.todocodeacademyEIMS.Ventas.mapper.Mapper;
import com.todocodeacademyEIMS.Ventas.model.Venta;
import com.todocodeacademyEIMS.Ventas.repository.CarritoAPI;
import com.todocodeacademyEIMS.Ventas.repository.IVentaRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService implements IVentaService {

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private CarritoAPI carritoAPI;

    @Override
    @Transactional(readOnly = true)
    public List<VentaDTO> findAll() {
        return ventaRepository.findAll()
                .stream()
                .map(Mapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VentaDTO findById(Long idVenta) {
        return Mapper.mapToDTO(findEntityById(idVenta));
    }

    @Override
    @Transactional
    public VentaDTO confirmarVenta(CreateVentaRequestDTO request) {
        CarritoDTO carrito = findCarritoById(request.getIdCarrito());

        if (carrito.getProductList() == null || carrito.getProductList().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede vender un carrito vacio");
        }

        Venta venta = Venta.builder()
                .idCarrito(carrito.getIdCarrito())
                .fechaVenta(LocalDateTime.now())
                .build();

        venta.setItems(Mapper.mapItemsFromCarrito(carrito, venta));

        // Amounts come already calculated from carrito-service (its recalculate runs
        // server-side): Ventas only freezes the total, it does not recompute it.
        venta.setTotal(carrito.getTotalPrice());

        return Mapper.mapToDTO(ventaRepository.save(venta));
    }

    @Override
    @Transactional
    public void deleteById(Long idVenta) {
        if (!ventaRepository.existsById(idVenta)) {
            throw notFound(idVenta);
        }
        ventaRepository.deleteById(idVenta);
    }

    /**
     * Resuelve un carrito por id contra el microservicio Carrito.
     *
     * Carrito responde 404 cuando el id no existe, y ante un 404 Feign lanza
     * {@link FeignException.NotFound}: nunca devuelve null. Sin este catch la excepcion
     * sube sin mapear y el cliente recibe un 500 en lugar de un 404.
     *
     * TODO: envolver esta llamada con un Circuit Breaker (Resilience4j). Cuando se haga,
     * el fallback debe ignorar NotFound (ignoreExceptions = FeignException.NotFound):
     * un carrito inexistente es un error del cliente, no una caida del servicio.
     */
    private CarritoDTO findCarritoById(Long idCarrito) {
        try {
            return carritoAPI.findCarritoById(idCarrito);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Carrito not found with id: " + idCarrito);
        }
    }

    private Venta findEntityById(Long idVenta) {
        return ventaRepository.findById(idVenta)
                .orElseThrow(() -> notFound(idVenta));
    }

    private ResponseStatusException notFound(Long idVenta) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta not found with id: " + idVenta);
    }
}
