package com.factugest.service;

import com.factugest.entity.Empresa;
import com.factugest.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {
    @Autowired private EmpresaRepository repo;

    public List<Empresa> getAll() { return repo.findAll(); }
    public Optional<Empresa> getById(Integer id) { return repo.findById(id); }
    public Empresa save(Empresa e) { return repo.save(e); }
    public void delete(Integer id) { repo.deleteById(id); }
}
