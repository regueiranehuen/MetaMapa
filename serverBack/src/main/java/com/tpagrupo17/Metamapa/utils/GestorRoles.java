package com.tpagrupo17.Metamapa.utils;

import com.tpagrupo17.Metamapa.entities.DbMain.usuario.Rol;
import com.tpagrupo17.Metamapa.entities.DbMain.usuario.Usuario;

import java.util.Objects;

public class GestorRoles {

public static Boolean VisualizadorAContribuyente(Usuario usuario){

    if (!Objects.isNull(usuario) && usuario.getRol().equals(Rol.VISUALIZADOR)) {

        // Cambio el "rol" a contribuyente
        usuario.setRol(Rol.CONTRIBUYENTE);

        return true;
    }

        return false;
}

public static Boolean ContribuyenteAVisualizador(Usuario usuario){

    if (usuario.getCantHechosSubidos() == 0) {

        // CAMBIAR ROL A VISUALIZADOR
        usuario.setRol(Rol.VISUALIZADOR);

        return true;
    }

    return false;
}

}
