package com.sistemabancario02.banco02.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

//ANOTACIONES PARA GETTERS SETTER AUTOMATICOS PARA UN CODIGO MAS LIMPIO
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String numeroCuenta;
    private double saldo;
    @Column(unique = true)
    private String identificacionCliente;
    //Enum de tipo de cuenta
    @Enumerated(EnumType.STRING)

    private TipoCuenta tipoCuenta;
    //Relación uno-a-muchos con Movimiento
    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Movimiento> movimientos;
    // Relación muchos-a-uno con Cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;




}

