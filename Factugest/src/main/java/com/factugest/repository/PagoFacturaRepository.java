package com.factugest.repository;

import com.factugest.entity.PagoFactura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoFacturaRepository extends JpaRepository<PagoFactura, Integer> {
}
