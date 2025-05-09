package com.sistemabancario02.banco02.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
 
import com.sistemabancario02.banco02.entity.Cuenta;
import com.sistemabancario02.banco02.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCuenta(Cuenta numeroCuenta);
    List<Movimiento> findByCuentaOrderByFechaDesc(Cuenta cuenta);
}
