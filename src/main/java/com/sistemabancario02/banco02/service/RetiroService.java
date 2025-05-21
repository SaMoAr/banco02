package com.sistemabancario02.banco02.service;

import com.sistemabancario02.banco02.entity.Cliente;
import com.sistemabancario02.banco02.entity.Cuenta;
import com.sistemabancario02.banco02.entity.TipoMovimiento;
import com.sistemabancario02.banco02.repository.ClienteRepository;
import com.sistemabancario02.banco02.repository.CuentaRepository;
import com.sistemabancario02.banco02.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import com.sistemabancario02.banco02.service.MovimientoService;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetiroService {
    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoService movimientoService;
    private final ClienteRepository clienteRepository;


    //Metodo basico para realizar un retiro en una cuenta, no tiene alguna verificacion aún
    public boolean realizarRetiro(Cuenta cuenta, double monto){
        //Se verifica que el monto no sea mayor a la cantidad de saldo en la cuenta y mayor que cero
        if (cuenta.getSaldo() >= monto && monto > 0){
            cuenta.setSaldo(cuenta.getSaldo() - monto);
            cuentaRepository.save(cuenta);
            movimientoService.registrarMovimiento(cuenta, monto , TipoMovimiento.RETIRO);
            return true;
        } else {
            return false;

        }
    }

    public boolean realizarRetiroVerificado(String identificacion, String numeroCuenta, double monto) {
        Cliente cliente = clienteRepository.findByIdentificacion(identificacion)
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
    
}
