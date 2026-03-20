package com.factugest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductDiscountService {
    @Autowired private JdbcTemplate jdbc;

    public List<Map<String, Object>> getAll() {
        return jdbc.queryForList("""
            SELECT p.nombre AS nombre_producto, d.descripcion AS descuento_aplicado, d.porcentaje
            FROM productos p
            JOIN producto_descuento pd ON p.cod_producto = pd.cod_producto
            JOIN descuentos d ON pd.cod_descuento = d.cod_descuento
            WHERE d.aplica_a_producto = 1
            """);
    }
}
