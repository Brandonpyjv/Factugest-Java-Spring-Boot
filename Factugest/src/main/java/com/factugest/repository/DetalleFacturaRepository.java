package com.factugest.repository;

import com.factugest.entity.DetalleFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Integer> {
    List<DetalleFactura> findByCodFactura(Integer codFactura);
    void deleteByCodFactura(Integer codFactura);
}
