package com.factugest.repository;

import com.factugest.entity.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImpuestoRepository extends JpaRepository<Impuesto, Integer> {
}
