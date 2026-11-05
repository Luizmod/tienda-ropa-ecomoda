package com.tienda.service;

public class CredencialesException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public CredencialesException(String clave) {
        super(clave);
    }
}
