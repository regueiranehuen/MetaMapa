package com.tpagrupo17.Metamapa.entities.atributosHecho;

public enum Origen {
    FUENTE_DINAMICA(0),
    FUENTE_ESTATICA(1),
    CARGA_MANUAL(2),
    FUENTE_PROXY_METAMAPA(3);

    private final Integer codigo;

    Origen(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String codigoEnString() {
        return this.name();
    }

    public static Origen fromCodigo(Integer codigo) {
        for (Origen origen : Origen.values()) {
            if (origen.getCodigo().equals(codigo)) {
                return origen;
            }
        }
        throw new IllegalArgumentException("Código de rol inválido: " + codigo);
    }

}
