package com.tpagrupo17.Metamapa.dtos.output;

import lombok.Getter;
import lombok.Setter;
import com.tpagrupo17.Metamapa.entities.usuario.Rol;

@Getter
@Setter
public class RolCambiadoDTO {
    String username;
    Rol rol;
    Boolean rolModificado;
}
