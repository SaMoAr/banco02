package com.sistemabancario02.banco02.dto;

import lombok.Data;


@Data
public class TransferenciaForm {
    private String cuentaorigen;
    private String cuentaDestino;
    private double monto;


}
