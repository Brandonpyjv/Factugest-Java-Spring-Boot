package com.factugest.service;

import com.factugest.entity.Usuario;
import com.factugest.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    @Autowired private UsuarioRepository repo;

    public List<Usuario> getAll() { return repo.findAll(); }
    public Optional<Usuario> getById(Integer id) { return repo.findById(id); }
    public Usuario save(Usuario u) { return repo.save(u); }
    public void delete(Integer id) { repo.deleteById(id); }
}
