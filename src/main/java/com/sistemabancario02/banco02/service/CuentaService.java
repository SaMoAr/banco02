package com.sistemabancario02.banco02.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sistemabancario02.banco02.repository.*;
import com.sistemabancario02.banco02.entity.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CuentaService {
     private final CuentaRepository cuentaRepository;

     //Metodo para crear cuentas que son accedidas por medio del Repositorio de dicha clase
     public Cuenta crearCuenta( Cliente cliente, String numeroCuenta, TipoCuenta tipoCuenta, double saldo){
         Cuenta cuenta = Cuenta.builder()
                 .cliente(cliente)
                 .numeroCuenta(numeroCuenta)
                 .tipoCuenta(tipoCuenta)
                 .saldo(saldo)
                 .build();
         return cuentaRepository.save(cuenta);
     }


    //Se define opcional el buscar una cuenta por numeroCuenta de cuenta
    public Optional<Cuenta> buscarPorNumero(String numeroCuenta){
        return cuentaRepository.findByNumeroCuenta(numeroCuenta);
    }

    //Se consulta el saldo de Cuenta por medio del getter saldo
    public double consultarSaldo(Cuenta cuenta){
        return cuenta.getSaldo();
    }

    //Se obtiene la lista de cuentas con el metodo getCuentas
    public List<Cuenta> obtenerCuentasCliente(Cliente cliente){
        return cliente.getCuentas(); 
    }

    //Metodo actualizar saldo con metodo Setter para el saldo a la instancia cuenta y se guarda el cambio
    public void actualizarSaldo(Cuenta cuenta, double nuevoSaldo) {
        cuenta.setSaldo(nuevoSaldo);
        cuentaRepository.save(cuenta);
    }

    //Se accede a la lista de cuentas y se busca por clientes con el repositorio de cuenta
    public List<Cuenta> buscarPorCliente(Cliente cliente) {
        return cuentaRepository.findByCliente(cliente);
    }

    //Metodo en progreso
    public Cuenta obtenerCuentaPorClienteActual(String username){
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
