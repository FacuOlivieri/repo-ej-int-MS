package com.todocodeacademyEIMS.Ventas.mapper;

import com.todocodeacademyEIMS.Ventas.dto.CarritoDTO;
import com.todocodeacademyEIMS.Ventas.dto.ProductDTO;
import com.todocodeacademyEIMS.Ventas.dto.ProductItemDTO;
import com.todocodeacademyEIMS.Ventas.dto.VentaDTO;
import com.todocodeacademyEIMS.Ventas.dto.VentaItemDTO;
import com.todocodeacademyEIMS.Ventas.model.Venta;
import com.todocodeacademyEIMS.Ventas.model.VentaItem;

import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static VentaDTO mapToDTO(Venta venta) {
        List<VentaItemDTO> items = venta.getItems() == null
                ? List.of()
                : venta.getItems().stream().map(Mapper::mapToDTO).toList();

        return VentaDTO.builder()
                .idVenta(venta.getIdVenta())
                .idCarrito(venta.getIdCarrito())
                .fechaVenta(venta.getFechaVenta())
                .items(items)
                .total(venta.getTotal())
                .build();
    }

    public static VentaItemDTO mapToDTO(VentaItem ventaItem) {
        return VentaItemDTO.builder()
                .idVentaLine(ventaItem.getIdVentaLine())
                .idProduct(ventaItem.getIdProduct())
                .name(ventaItem.getName())
                .brand(ventaItem.getBrand())
                .quantity(ventaItem.getQuantity())
                .unitPrice(ventaItem.getUnitPrice())
                .subtotal(ventaItem.getSubtotal())
                .build();
    }

    /**
     * Converts the carrito-service response into the sale lines (snapshot).
     * Amounts are copied as-is: carrito-service is the price owner and Ventas does
     * not recalculate them.
     */
    public static List<VentaItem> mapItemsFromCarrito(CarritoDTO carrito, Venta venta) {
        List<VentaItem> items = new ArrayList<>();

        if (carrito.getProductList() == null) {
            return items;
        }

        for (ProductItemDTO productItemDTO : carrito.getProductList()) {
            ProductDTO product = productItemDTO.getProduct();

            VentaItem item = VentaItem.builder()
                    .idProduct(product == null ? null : product.getIdProduct())
                    .name(product == null ? null : product.getName())
                    .brand(product == null ? null : product.getBrand())
                    .quantity(productItemDTO.getQuantity())
                    .unitPrice(product == null ? 0d : product.getUnitPrice())
                    .subtotal(productItemDTO.getSubtotal())
                    .build();

            // Back-reference: without it the @ManyToOne stays null
            // and the idVenta foreign key is never persisted.
            item.setVenta(venta);
            items.add(item);
        }

        return items;
    }
}
