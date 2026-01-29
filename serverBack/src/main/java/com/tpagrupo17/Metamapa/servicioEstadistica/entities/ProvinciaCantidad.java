package com.tpagrupo17.Metamapa.servicioEstadistica.entities;

import lombok.Data;

@Data
public class ProvinciaCantidad {
    private Long provincia_id;
    private Long cantidad;

    public ProvinciaCantidad(Long provincia_id) {
        this.provincia_id = provincia_id;
        this.cantidad = 0L;
    }

    public void incrementarCantidad(){
        this.cantidad += 1L;
    }
}
