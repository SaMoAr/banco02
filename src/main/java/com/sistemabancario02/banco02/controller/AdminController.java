package com.sistemabancario02.banco02.controller;

import com.sistemabancario02.banco02.entity.Cliente;
import com.sistemabancario02.banco02.entity.Cuenta;
import com.sistemabancario02.banco02.entity.TipoCuenta;
import com.sistemabancario02.banco02.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ClienteService clienteService;
    private final CuentaService cuentaService;

    /**
     * Muestra la página principal del panel de administración
     */
    @GetMapping({"", "/"})
    public String adminHome(Model model) {
        return "admin/index";
    }

    @GetMapping("/index")
    public String index(Model model) {
        return "admin/index";
    }


    /**
     * Muestra el formulario para crear un nuevo cliente
     */
    @GetMapping("/crear-cliente")
    public String mostrarFormularioCrearCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "admin/crear-cliente";
    }

    /**
     * Procesa la creación de un nuevo cliente
     */
    @PostMapping("/crear-cliente")
    public String crearCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        try {
            clienteService.crearCliente(cliente);
            redirectAttributes.addFlashAttribute("mensaje", "Cliente creado exitosamente");
            redirectAttributes.addFlashAttribute("error", false);
            return "redirect:/admin/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear cliente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", true);
            return "redirect:/admin/crear-cliente";
        }
    }

    /**
     * Muestra el formulario para crear una nueva cuenta
     */
    @GetMapping("/crear-cuenta")
    public String mostrarFormularioCrearCuenta(Model model) {
        model.addAttribute("tiposCuenta", TipoCuenta.values());
        return "admin/crear-cuenta";
    }

    /**
     * Procesa la creación de una nueva cuenta
     */
    @PostMapping("/crear-cuenta")
    public String crearCuenta(@RequestParam String identificacionCliente,
                             @RequestParam String numeroCuenta,
                             @RequestParam TipoCuenta tipoCuenta,
                             @RequestParam double saldo,
                             RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.buscarPorIdentificacionCliente(identificacionCliente)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            cuentaService.crearCuenta(cliente, numeroCuenta, tipoCuenta, saldo);
            redirectAttributes.addFlashAttribute("mensaje", "Cuenta creada exitosamente");
            redirectAttributes.addFlashAttribute("error", false);
            return "redirect:/admin/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear cuenta: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", true);
            return "redirect:/admin/crear-cuenta";
        }
    }

    /**
     * Muestra el formulario para desbloquear un cliente
     */
    @GetMapping("/desbloquear")
    public String mostrarFormularioDesbloqueo(Model model) {
        return "admin/desbloquear";
    }

    /**
     * Procesa el desbloqueo de un cliente
     */
    @PostMapping("/desbloquear")
    public String desbloquear(@RequestParam String identificacionCliente,
                             @RequestParam String nuevoPin,
                             RedirectAttributes redirectAttributes) {
        try {
            Cliente cliente = clienteService.buscarPorIdentificacionCliente(identificacionCliente)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            if (!cliente.isBloqueado()) {
                throw new RuntimeException("El cliente no está bloqueado");
            }

            clienteService.desbloquearCliente(identificacionCliente, nuevoPin);
            redirectAttributes.addFlashAttribute("mensaje", "Cliente desbloqueado exitosamente");
            redirectAttributes.addFlashAttribute("error", false);
            return "redirect:/admin/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al desbloquear cliente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", true);
            return "redirect:/admin/desbloquear";
        }
    }

    /**
     * Redirige a la página de inicio del admin cuando se accede a la ruta base
     */
    @GetMapping("")
    public String redirectToHome() {
        return "redirect:/admin/";
    }
}