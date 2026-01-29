package com.tpagrupo17.Metamapa.entities.DbMain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Hash {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Genera el hash de la contraseña
    public static String generarHash(String passwordPlano) {
        return encoder.encode(passwordPlano);
    }

    // Verifica si una contraseña en texto plano coincide con el hash
    public static boolean verificarPassword(String passwordPlano, String hashGuardado) {
        return encoder.matches(passwordPlano, hashGuardado);
    }

}
