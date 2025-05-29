package com.sistemabancario02.banco02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.sistemabancario02.banco02.entity.Cliente;


public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional <Cliente> findByIdentificacionCliente(String identificacionCliente);
}
