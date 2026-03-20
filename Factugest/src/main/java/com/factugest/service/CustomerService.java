package com.factugest.service;

import com.factugest.entity.Customer;
import com.factugest.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository repo;

    public List<Customer> getAll() { return repo.findAllByOrderByFullNameAsc(); }
    public Optional<Customer> getById(Integer id) { return repo.findById(id); }
    public Customer save(Customer c) { return repo.save(c); }
    public void delete(Integer id) { repo.deleteById(id); }
}
