package com.sistemabancario02.banco02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sistemabancario02.banco02.entity.Cuenta;
import java.util.Optional;
import java.util.List;
import com.sistemabancario02.banco02.entity.Cliente;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    List<Cuenta> findByCliente(Cliente cliente);

}
