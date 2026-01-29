package com.tpagrupo17.Metamapa.entities.atributosHecho;

public enum OrigenConexion {
    INVALIDO(-1),
    FRONT(0),
    PROXY(1);

    private final Integer codigo;

    OrigenConexion(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String codigoEnString() {
        return this.name();
    }

    public static OrigenConexion fromCodigo(Integer codigo) {
        for (OrigenConexion origen : OrigenConexion.values()) {
            if (origen.getCodigo().equals(codigo)) {
                return origen;
            }
        }
        throw new IllegalArgumentException("Código de rol inválido: " + codigo);
    }
}
