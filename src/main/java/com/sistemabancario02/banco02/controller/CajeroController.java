package com.sistemabancario02.banco02.controller;

import java.util.Map;
import java.util.Optional;

import com.sistemabancario02.banco02.dto.TransferenciaForm;
import com.sistemabancario02.banco02.entity.Cliente;
import com.sistemabancario02.banco02.entity.Cuenta;
import com.sistemabancario02.banco02.service.ClienteService;
import com.sistemabancario02.banco02.service.CuentaService;
import com.sistemabancario02.banco02.service.MovimientoService;
import com.sistemabancario02.banco02.service.RetiroService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/cajero") // Mapeo base para todas las rutas de este controlador
public class CajeroController {

    private final ClienteService clienteService;
    private final CuentaService cuentaService;
    private final MovimientoService movimientoService;
    private final RetiroService retiroService;

    // --- Métodos de Login y Logout ---

    /**
     * Muestra el formulario de inicio de sesión del cajero.
     * Los 'flash attributes' (como 'error' o 'mensaje') de una redirección previa
     * se añaden automáticamente al Model para esta solicitud.
     */
    @GetMapping("/login")
    public String mostrarLogin(Model model) {
    return "cajero/login";  // Correct, maintains consistency
}

    /**
     * Procesa la solicitud de inicio de sesión (POST).
     * Valida las credenciales, maneja bloqueos de cuenta y gestiona intentos fallidos.
     * Almacena el cliente autenticado en la sesión y redirige al menú principal.
     */
    @PostMapping("/login")
    public String login(@RequestParam String numeroCuenta,
                        @RequestParam String pin,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        Optional<Cuenta> cuentaOptional = cuentaService.buscarPorNumero(numeroCuenta);

        // 1. Verificar si la cuenta existe
        if (cuentaOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cuenta no encontrada.");
            return "redirect:/cajero/login";
        }

        Cuenta cuenta = cuentaOptional.get();
        Cliente cliente = cuenta.getCliente();

        // 2. Verificar si el cliente está bloqueado
        if (cliente.isBloqueado()) {
            redirectAttributes.addFlashAttribute("error", "Cuenta bloqueada. Contacte a su banco.");
            return "redirect:/cajero/login";
        }

        // 3. Verificar el PIN (ADVERTENCIA: Considera implementar HASHING para seguridad real)
        if (!cliente.getPin().equals(pin)) {
            clienteService.incrementarIntento(cliente);

            if (cliente.getIntentosFallidos() >= 3) {
                clienteService.bloquearCliente(cliente);
                redirectAttributes.addFlashAttribute("error", "Cuenta bloqueada por múltiples intentos fallidos.");
            } else {
                redirectAttributes.addFlashAttribute("error", "PIN incorrecto. Intentos restantes: " + (3 - cliente.getIntentosFallidos()));
            }
            return "redirect:/cajero/login";
        }

        // 4. Si el login es exitoso: reiniciar intentos y guardar cliente en sesión
        clienteService.reiniciarIntentos(cliente);
        session.setAttribute("cliente", cliente);

        return "redirect:/cajero/menu";
    }

    /**
     * Invalida la sesión del usuario y lo redirige a la página de login.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/cajero/login";
    }

    // --- Métodos de Navegación del Menú Principal ---

    /**
     * Muestra el menú principal del cajero.
     * Requiere que el cliente esté autenticado en la sesión.
     */
    @GetMapping("/menu")
    public String menu(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cajero/login";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("cuentas", cuentaService.buscarPorCliente(cliente));
        return "cajero/menu";
    }

    // --- Métodos de Consultas ---

    /**
     * Muestra la página de consultas de cuentas.
     * Requiere autenticación.
     */
    @GetMapping("/consultas")
    public String consultas(Model model, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cajero/login"; // Faltaba el slash inicial
        }
        model.addAttribute("cuentas", cuentaService.buscarPorCliente(cliente));
        return "cajero/consultas";
    }

    /**
     * Muestra los movimientos de una cuenta específica.
     * Requiere autenticación.
     */
    @GetMapping("/movimientos/{numero}")
    public String movimientos(@PathVariable String numero, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Cliente clienteSesion = (Cliente) session.getAttribute("cliente");
        if (clienteSesion == null) {
            return "redirect:cajero/login";
        }

        try {
            Optional<Cuenta> cuentaOptional = cuentaService.buscarPorNumero(numero);

            if (cuentaOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Cuenta no encontrada.");
                return "redirect:/ajero/consultas";
            }

            Cuenta cuenta = cuentaOptional.get();
            Cliente clienteDeLaCuenta = cuenta.getCliente();

            // LÍNEA CLAVE: ASEGÚRATE DE QUE getId() DEVUELVA 'Long' (objeto) y no 'long' (primitivo)
            // Si getId() devuelve 'Long', esta línea es correcta.
            // Si getId() devuelve 'long', DEBERÁS CAMBIAR A: clienteDeLaCuenta.getId() != clienteSesion.getId()
            // Y SIEMPRE ASEGURARTE QUE NINGUNO ES NULL ANTES DE ACCEDER A getId()
           /* if (clienteDeLaCuenta == null || !clienteDeLaCuenta.getId().equals(clienteSesion.getId())) { //
                redirectAttributes.addFlashAttribute("error", "Cuenta no encontrada o no pertenece a su usuario.");
                return "redirect:/cajero/consultas";
            }*/

            var movimientos = movimientoService.buscarPorCuenta(numero);
            model.addAttribute("movimientos", movimientos);
            model.addAttribute("numeroCuentaActual", numero);
            return "cajero/movimientos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No fue posible obtener los movimientos: " + e.getMessage());
            return "redirect:cajero/consultas";
        }
    }

    // --- Métodos de Retiro ---

    /**
     * Muestra el formulario para realizar retiros.
     * Requiere autenticación.
     */
    @GetMapping("/retiro")
    public String mostrarFormularioRetiro(Model model, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cajero/login";
        }

        // Agregamos tanto el cliente como sus cuentas al modelo
        model.addAttribute("cliente", cliente);
        model.addAttribute("cuentas", cuentaService.buscarPorCliente(cliente));
        return "cajero/retiro";
    }

    @PostMapping("/retiro")
    public String realizarRetiro(@RequestParam String identificacionCliente,
                                 @RequestParam String numeroCuenta,
                                 @RequestParam double monto,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Verificar que el cliente en sesión coincida con la identificación
            Cliente clienteSesion = (Cliente) session.getAttribute("cliente");
            if (clienteSesion == null || !clienteSesion.getIdentificacionCliente().equals(identificacionCliente)) {
                redirectAttributes.addFlashAttribute("error", "Sesión inválida o datos inconsistentes.");
                return "redirect:/cajero/login";
            }

            retiroService.realizarRetiro(identificacionCliente, numeroCuenta, monto);
            redirectAttributes.addFlashAttribute("mensaje", "Retiro exitoso.");
            return "redirect:/cajero/menu";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cajero/retiro";
        }
    }


    // --- Métodos de Consignación ---

    /**
     * Muestra el formulario para realizar consignaciones.
     * Requiere autenticación.
     */
    @GetMapping("/consignar")
    public String mostrarFormularioConsignacion(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cajero/login";
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("cuentas", cuentaService.buscarPorCliente(cliente));
        return "cajero/consignar";
    }


    /**
     * Procesa la solicitud de consignación (POST).
     */
    @PostMapping("/consignar")
    public String consignar(@RequestParam String numeroCuenta,
                            @RequestParam double monto,
                            RedirectAttributes redirectAttributes) {
        try {
            Cuenta cuenta = cuentaService.buscarPorNumero(numeroCuenta)
                    .orElseThrow(() -> new RuntimeException("Cuenta de destino no encontrada."));

            movimientoService.realizarConsignacion(cuenta, monto);
            redirectAttributes.addFlashAttribute("mensaje", "Consignación exitosa. Nuevo saldo: " + cuenta.getSaldo());
            return "redirect:/cajero/menu";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al consignar: " + e.getMessage());
        }
        return "redirect:/cajero/consignar";
    }

    // --- Métodos de Transferencia ---

    /**
     * Muestra el formulario para realizar transferencias.
     * Requiere autenticación.
     */
    @GetMapping("/transferir")
    public String mostrarFormularioTransferencia(Model model, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cajero/login";
        }
        model.addAttribute("transferenciaForm", new TransferenciaForm());
        model.addAttribute("cuentasOrigen", cuentaService.buscarPorCliente(cliente));
        return "cajero/transferir";
    }

    /**
     * Procesa la solicitud de transferencia (POST).
     * Requiere seleccionar una cuenta origen del cliente logueado y una cuenta destino.
     */
    @PostMapping("/transferir")
    public String transferir(@RequestParam String numeroCuentaOrigen,
                             @RequestParam String numeroCuentaDestino,
                             @RequestParam double monto,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cajero/login";
        }

        try {
            Cuenta origen = cuentaService.buscarPorNumero(numeroCuentaOrigen)
                    .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada."));
    /*
            if (!origen.getCliente().getId().equals(cliente.getId())) {
                throw new RuntimeException("La cuenta origen no pertenece a su usuario.");
            } */

            Cuenta destino = cuentaService.buscarPorNumero(numeroCuentaDestino)
                    .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada."));

            if (movimientoService.realizarTransferencia(origen, monto, destino)) {
                redirectAttributes.addFlashAttribute("mensaje", "Transferencia realizada con éxito.");
                return "redirect:/cajero/menu";
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo realizar la transferencia (ej. saldo insuficiente).");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error en la transferencia: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al transferir: " + e.getMessage());
        }

        return "redirect:/cajero/transferir";
    }

    // --- Métodos para Obtener Titular (Generalmente para AJAX) ---

    /**
     * Endpoint para obtener el nombre del titular de una cuenta por su número.
     * Útil para funcionalidades AJAX (ej. autocompletar en transferencias).
     */
    @GetMapping("/titular")
    @ResponseBody
    public Map<String, String> obtenerTitular(@RequestParam String numero){
        return  cuentaService.buscarPorNumero(numero)
                .map(cuenta -> Map.of("nombre", cuenta.getCliente().getNombre()))
                .orElse(Map.of());
    }

    // --- Métodos de Cambio de Clave ---

    /**
     * Muestra el formulario para cambiar la clave (PIN) del cliente.
     * Requiere autenticación.
     */
    @GetMapping("/cambiar-clave")
    public String mostrarFormularioCambioClave(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/cajero/login";  // Corregido el redirect
        }
        model.addAttribute("cliente", cliente);
        return "cajero/cambiar-clave";
    }


    /**
     * Procesa la solicitud de cambio de clave (POST).
     * Valida la clave actual y la confirmación de la nueva clave.
     */
    @PostMapping("/cambiar-clave")
    public String cambiarClave(@RequestParam String claveActual,
                               @RequestParam String nuevaClave,
                               @RequestParam String confirmarClave,
                               HttpSession session,
                               RedirectAttributes redirectAttributes){
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null){
        return "redirect:/cajero/login";  // Fixed redirect path
    }

        // 1. Verificar clave actual (ADVERTENCIA: Considerar HASHING)
        if (!cliente.getPin().equals(claveActual)) {
            redirectAttributes.addFlashAttribute("error", "Clave actual incorrecta.");
            return "redirect:/cajero/cambiar-clave";
        }

        // 2. Verificar que las nuevas claves coincidan
        if (!nuevaClave.equals(confirmarClave)) {
            redirectAttributes.addFlashAttribute("error", "Las nuevas claves no coinciden.");
            return "redirect:/cajero/cambiar-clave";
        }

        // 3. Cambiar el PIN a través del servicio
        try {
            clienteService.cambiarPin(cliente, nuevaClave);
            session.setAttribute("cliente", cliente);
            redirectAttributes.addFlashAttribute("mensaje", "Clave cambiada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar la clave: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al cambiar la clave.");
        }

        return "redirect:/cajero/cambiar-clave";
    }
}