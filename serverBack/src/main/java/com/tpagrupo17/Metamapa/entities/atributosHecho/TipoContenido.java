package com.tpagrupo17.Metamapa.entities.atributosHecho;

public enum TipoContenido {
    INVALIDO(-1),
    TEXTO(0),
    IMAGEN(1),
    AUDIO(2),
    VIDEO(3);

    private final Integer codigo;

    TipoContenido(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String codigoEnString() {
        return this.name();
    }

    public static TipoContenido fromCodigo(Integer codigo) {
        for (TipoContenido tipoContenido : TipoContenido.values()) {
            if (tipoContenido.getCodigo().equals(codigo)) {
                return tipoContenido;
            }
        }
        throw new IllegalArgumentException("Código de rol inválido: " + codigo);
    }

}

