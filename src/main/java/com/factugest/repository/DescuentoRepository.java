package com.factugest.repository;

import com.factugest.entity.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DescuentoRepository extends JpaRepository<Descuento, Integer> {
}
