package com.factugest.service;

import com.factugest.entity.Producto;
import com.factugest.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductoService {
    @Autowired private ProductoRepository repo;
    @Autowired private JdbcTemplate jdbc;

    public List<Map<String, Object>> getAllDetailed() {
        return jdbc.queryForList("""
            SELECT p.cod_producto, p.sku, p.nombre, p.descripcion, p.precio_unitario,
                   p.stock, p.stock_minimo, p.cod_impuesto, p.unidad_medida,
                   p.codigo_barras, p.activo,
                   i.descripcion AS tax_name,
                   COALESCE(i.porcentaje, 0) AS tax_porcentaje
            FROM productos p LEFT JOIN impuestos i ON p.cod_impuesto = i.cod_impuesto
            ORDER BY p.nombre
            """);
    }

    public List<Producto> getAll() { return repo.findAllByOrderByNombreAsc(); }
    public Optional<Producto> getById(Integer id) { return repo.findById(id); }
    public Producto save(Producto p) { return repo.save(p); }
    public void delete(Integer id) { repo.deleteById(id); }
}
