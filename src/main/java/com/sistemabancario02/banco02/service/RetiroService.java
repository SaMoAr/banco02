// En tu RetiroService.java (este es un ejemplo de cómo debería ser, no el controlador)
package com.sistemabancario02.banco02.service;

import com.sistemabancario02.banco02.entity.Cliente;
import com.sistemabancario02.banco02.entity.Cuenta;
import com.sistemabancario02.banco02.entity.Movimiento;
import com.sistemabancario02.banco02.repository.ClienteRepository;
import com.sistemabancario02.banco02.repository.CuentaRepository;
import com.sistemabancario02.banco02.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.sistemabancario02.banco02.entity.TipoMovimiento.RETIRO;

@Service
@RequiredArgsConstructor
public class RetiroService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ClienteRepository clienteRepository;
    // Podrías necesitar ClienteRepository si la validación de identificacion se hace aquí

    @Transactional // Hace que el método sea una transacción atómica
    public void realizarRetiro(String identificacionCliente, String numeroCuenta, double monto) {

        if (monto <= 0) {
            throw new RuntimeException("El monto del retiro debe ser mayor a cero.");
        }

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada."));

        // **IMPORTANTE**: Aquí deberías verificar que 'identificacionCliente' realmente
        // corresponde al 'cliente' de la 'cuenta'. Esto es una validación de seguridad.
        if (cuenta.getCliente() == null || !cuenta.getCliente().getIdentificacionCliente().equals(identificacionCliente)) {
            throw new RuntimeException("La cuenta no está asociada a la identificación proporcionada.");
        }


        if (cuenta.getSaldo() < monto) {
            throw new RuntimeException("Saldo insuficiente en la cuenta " + numeroCuenta + ". Saldo actual: " + cuenta.getSaldo());
        }

        cuenta.setSaldo(cuenta.getSaldo() - monto);
        cuentaRepository.save(cuenta); // Guarda el nuevo saldo

        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setTipo(RETIRO);
        movimiento.setMonto(monto);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);
    }
}