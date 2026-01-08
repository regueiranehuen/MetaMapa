package raiz.Front;

public enum TipoContenido {
    INVALIDO(-1),
    TEXTO(0),
    IMAGEN(1),
    AUDIO(2),
    VIDEO(3);

    private final int codigo;

    TipoContenido(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public String codigoEnString() {
        return this.name();
    }

    public static TipoContenido fromCodigo(int codigo) {
        for (TipoContenido tipoContenido : TipoContenido.values()) {
            if (tipoContenido.getCodigo() == codigo) {
                return tipoContenido;
            }
        }
        throw new IllegalArgumentException("Código de rol inválido: " + codigo);
    }

}