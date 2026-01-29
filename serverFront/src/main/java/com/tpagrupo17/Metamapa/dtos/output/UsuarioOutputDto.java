package com.tpagrupo17.Metamapa.dtos.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.tpagrupo17.Metamapa.entities.usuario.Rol;

@Data
@AllArgsConstructor
public class UsuarioOutputDto {
    private Long id;
    private String nombreDeUsuario;
    private String nombre;
    private String apellido;
    private Integer edad;
    private Integer cantHechosSubidos;
    private Rol rol;
    public UsuarioOutputDto(){}
}
