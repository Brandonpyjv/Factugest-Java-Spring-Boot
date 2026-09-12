package com.factugest.repository;

import com.factugest.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositorio JPA del catálogo de productos.
 *
 * JpaRepository<Producto, Integer>:
 *   - Producto = la entidad que gestiona
 *   - Integer  = tipo de la clave primaria (cod_producto)
 *
 * findAllByOrderByNombreAsc → SELECT * FROM productos ORDER BY nombre ASC
 * Se usa para cargar el catálogo completo en formularios donde no se necesita
 * el JOIN con impuestos (para eso existe ProductoService.getAllDetailed()).
 */
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findAllByOrderByNombreAsc();
}
