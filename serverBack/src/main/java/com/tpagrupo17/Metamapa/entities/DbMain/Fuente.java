package com.tpagrupo17.Metamapa.entities.DbMain;

public enum Fuente {
    DINAMICA(1),
    ESTATICA(2),
    PROXY(3);

    private Integer codigo;

    Fuente(Integer codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public String codigoEnString() {
        return this.name();
    }

    public static Fuente fromCodigo(int codigo) {
        for (Fuente fuente : Fuente.values()) {
            if (fuente.getCodigo() == codigo) {
                return fuente;
            }
        }
        throw new IllegalArgumentException("Código de rol inválido: " + codigo);
    }

}
