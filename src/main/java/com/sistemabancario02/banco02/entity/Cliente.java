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
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "nombre", unique = true, nullable = false)
    private String nombre;
    @Column(unique = true)
    private String identificacionCliente;
    private String pin;
    private boolean bloqueado;
    private int intentosFallidos;
    // Relación uno-a-muchos con cuentas
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cuenta> cuentas;
}
