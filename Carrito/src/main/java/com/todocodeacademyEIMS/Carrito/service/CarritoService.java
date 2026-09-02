package com.todocodeacademyEIMS.Carrito.service;

import com.todocodeacademyEIMS.Carrito.dto.CarritoDTO;
import com.todocodeacademyEIMS.Carrito.mapper.Mapper;
import com.todocodeacademyEIMS.Carrito.model.Carrito;
import com.todocodeacademyEIMS.Carrito.model.ProductItem;
import com.todocodeacademyEIMS.Carrito.repository.ICarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CarritoService implements ICarritoService {

    @Autowired
    private ICarritoRepository carritoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CarritoDTO> findAll() {
        return carritoRepository.findAll()
                .stream()
                .map(Mapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CarritoDTO findById(Long idCarrito) {
        return Mapper.mapToDTO(findEntityById(idCarrito));
    }

    @Override
    @Transactional
    public CarritoDTO save(CarritoDTO carritoDTO) {
        Carrito carrito = Mapper.mapFromDTO(carritoDTO);
        carrito.setIdCarrito(null);
        carrito.getProductList().forEach(productItem -> productItem.setIdProductLine(null));

        recalculate(carrito);

        return Mapper.mapToDTO(carritoRepository.save(carrito));
    }

    @Override
    @Transactional
    public CarritoDTO update(Long idCarrito, CarritoDTO carritoDTO) {
        Carrito carrito = findEntityById(idCarrito);
        Carrito incoming = Mapper.mapFromDTO(carritoDTO);

        // The managed collection is cleared and refilled instead of replaced:
        // orphanRemoval only deletes the old rows if Hibernate keeps tracking the same list.
        carrito.getProductList().clear();
        for (ProductItem productItem : incoming.getProductList()) {
            productItem.setIdProductLine(null);
            productItem.setCarrito(carrito);
            carrito.getProductList().add(productItem);
        }

        recalculate(carrito);

        return Mapper.mapToDTO(carritoRepository.save(carrito));
    }

    @Override
    @Transactional
    public void deleteById(Long idCarrito) {
        if (!carritoRepository.existsById(idCarrito)) {
            throw notFound(idCarrito);
        }
        carritoRepository.deleteById(idCarrito);
    }

    /**
     * Recalculates every subtotal and the cart total on the server.
     * Amounts are never taken from the request: a client could send unitPrice = 0.01.
     *
     * FEIGN SEAM: this is where the Producto microservice will be queried by idProduct
     * to fetch the real unit price and to validate that the product exists, instead of
     * trusting the unitPrice that arrives in the DTO.
     */
    private void recalculate(Carrito carrito) {
        double totalPrice = 0d;

        for (ProductItem productItem : carrito.getProductList()) {
            productItem.setSubtotal(productItem.getUnitPrice() * productItem.getQuantity());
            totalPrice += productItem.getSubtotal();
        }

        carrito.setTotalPrice(totalPrice);
    }

    private Carrito findEntityById(Long idCarrito) {
        return carritoRepository.findById(idCarrito)
                .orElseThrow(() -> notFound(idCarrito));
    }

    private ResponseStatusException notFound(Long idCarrito) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito not found with id: " + idCarrito);
    }
}
