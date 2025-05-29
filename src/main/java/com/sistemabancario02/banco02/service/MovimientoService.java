package com.sistemabancario02.banco02.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sistemabancario02.banco02.repository.*;
import com.sistemabancario02.banco02.entity.*;

import java.time.LocalDateTime;

import lombok.*;


@Service
@RequiredArgsConstructor
public class MovimientoService {


    //se inyectan los repositorios
    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;

    //Metodo para registrar movimientos en la base de datos
    public Movimiento registrarMovimiento(Cuenta cuenta, double monto, TipoMovimiento tipoMovimiento) {

        //el builder se encarga de crear el objeto movimiento con los datos ingresados por el usuario
        Movimiento movimiento = Movimiento.builder()
                .tipo(tipoMovimiento)
                .cuenta(cuenta)
                .monto(monto)
                .fecha(LocalDateTime.now())
                .build();
        return movimientoRepository.save(movimiento);

    }

    //Metodo para obtener los movimientos de una cuenta en especifico por medio de su número
    public List<Movimiento> obtenerMovimientosPorNumeroCuenta(Cuenta numeroCuenta) {
        return movimientoRepository.findByCuenta(numeroCuenta);

    }

    //Metodo basico para realizar un retiro en una cuenta, no tiene alguna verificacion aún
    public boolean realizarRetiro(Cuenta cuenta, double monto){
        //Se verifica que el monto no sea mayor a la cantidad de saldo en la cuenta y mayor que cero
        if (cuenta.getSaldo() >= monto && monto > 0){
            cuenta.setSaldo(cuenta.getSaldo() - monto);
            cuentaRepository.save(cuenta);
            registrarMovimiento(cuenta, monto ,TipoMovimiento.RETIRO);
            return true;
        } else {
            return false;

        } 
    }

    //Se declara el metodo que realiza las transferencias con los datos requeridos y la almacena en DB
    public boolean realizarTransferencia(Cuenta origen, double monto , Cuenta destino){
        //Se valida que el saldo sea mayor que el monto a transferir y que sea mayor que cero y que el destino sea diferente al origen
        if (origen.getSaldo() >= monto && monto > 0 && !origen.equals(destino)){
            origen.setSaldo(origen.getSaldo() - monto);
            destino.setSaldo(destino.getSaldo() + monto);
            cuentaRepository.save(origen);
            cuentaRepository.save(destino);
            registrarMovimiento(origen,-monto ,TipoMovimiento.TRANSFERENCIA ); /*Se registra el movimiento de salida,
            como se esta sacando monto del origen por eso aparece el argumento -monto*/
            registrarMovimiento(destino,monto , TipoMovimiento.TRANSFERENCIA); // se registra el movimiento de entrada,
            // como se esta ingresando monto al destino por eso aparece el argumento monto*/
            return true;
        } else {
            return false;
        }

    }

    //Se declara el metodo que busca en la lista de movimientos de una cuenta por medio de su numero de cuenta
    public List<Movimiento> buscarPorCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                //si no se encuentra la cuenta se lanza una excepcion de tipo runtime
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
                return movimientoRepository.findByCuentaOrderByFechaDesc(cuenta);
    }

    /*
    * Metodo para realizar una consignacion en una cuenta*/
    public boolean realizarConsignacion(Cuenta cuenta, double monto){
        if (monto <= 0){
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
        cuenta.setSaldo(cuenta.getSaldo() + monto);
        cuentaRepository.save(cuenta);
        registrarMovimiento(cuenta,monto , TipoMovimiento.CONSIGNACION);
        return true;

    }

    public boolean realizarRetiroVerificado(String identificacionCliente, String numeroCuenta, double monto) {
        Cliente cliente = clienteRepository.findByIdentificacionCliente(identificacionCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        if (!cuenta.getCliente().equals(cliente)) {
            throw new RuntimeException("La cuenta no pertenece al cliente");
        }
        if (cliente.isBloqueado()) {
            throw new RuntimeException("El cliente o su cuenta estan bloqueados");
        }

        // Call the simple withdrawal method after validations
        return realizarRetiro(cuenta, monto);
    }

    /*Funcionalidad de verificacion del exito del movimiento ( en proceso )....
    NOTAS A FUTURO;  ESTABLECER UNA CONSTANTE PARA ARROJAR UN MISMO ERROR. NO CREAR UN ERROR DIFERENTE PARA EL MISMO CASO
    boolean exito = movimientoService.realizarRetiro();
    */





}
