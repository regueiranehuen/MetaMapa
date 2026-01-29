package com.tpagrupo17.Metamapa.entities.DbMain.usuario;

public enum Rol {
    VISUALIZADOR(0),
    CONTRIBUYENTE(1),
    ADMINISTRADOR(2);

    private final int codigo;

    Rol(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static Rol fromCodigo(int codigo) {
        for (Rol rol : Rol.values()) {
            if (rol.getCodigo() == codigo) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Código de rol inválido: " + codigo);
    }

}
