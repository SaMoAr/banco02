package com.sistemabancario02.banco02.controller;

import com.sistemabancario02.banco02.entity.Cliente;
import com.sistemabancario02.banco02.entity.Cuenta;
import com.sistemabancario02.banco02.entity.TipoCuenta;
import com.sistemabancario02.banco02.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ClienteService clienteService;
    private final CuentaService cuentaService;

    @GetMapping
    public String adminHome(){

        return "admin/index";
    }

    @GetMapping("/crear-cliente")
    public String crearCliente(Model model){
        model.addAttribute("cliente", new Cliente());
        return "admin/crear-cliente";
    }

    @PostMapping("/crear-cliente")
    public String crearCliente(Cliente cliente){
        clienteService.crearCliente(cliente);
        return "redirect:/admin";
    }

    @GetMapping("/crear-cuenta")
    public String crearCuenta(Model model){
        model.addAttribute("cuenta", new Cuenta());
        return "admin/crear-cuenta";
    }

    @PostMapping("crear-cuenta")
    public String CrearCuentacontrol(@RequestParam String identificacion,
                              @RequestParam String numero,
                              @RequestParam TipoCuenta tipo,
                              @RequestParam double saldoInicial ){
        Cliente cliente = clienteService.buscarPorIdentificacion(identificacion).orElse(null);
        cuentaService.crearCuenta( numero, tipo, saldoInicial );
        return "redirect:/admin";
    }

    @GetMapping("/desbloquear")
    public String mostrarDesbloqueo(){
        return "admin/desbloquear";
    }

     @PostMapping("/desbloquear")
    public String desbloquear(@RequestParam String identificacion, @RequestParam String nuevoPin){
        return "redirect:/admin";
     }
}
