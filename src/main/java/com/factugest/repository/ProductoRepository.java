package com.factugest.repository;

import com.factugest.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findAllByOrderByNombreAsc();
}
