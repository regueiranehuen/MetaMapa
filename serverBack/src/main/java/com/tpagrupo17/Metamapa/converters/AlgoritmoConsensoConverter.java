package com.tpagrupo17.Metamapa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso.AlgoritmoConsensoMayoriaAbsoluta;
import com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso.AlgoritmoConsensoMayoriaSimple;
import com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso.AlgoritmoConsensoMultiplesMenciones;
import com.tpagrupo17.Metamapa.entities.DbMain.algoritmosConsenso.IAlgoritmoConsenso;

@Converter(autoApply = true)
public class AlgoritmoConsensoConverter implements AttributeConverter<IAlgoritmoConsenso, String> {

    // Códigos estables para persistir
    private static final String COD_MS  = "MAYORIA_SIMPLE";
    private static final String COD_MA  = "MAYORIA_ABSOLUTA";
    private static final String COD_MM  = "MULTIPLES_MENCIONES";

    @Override
    public String convertToDatabaseColumn(IAlgoritmoConsenso atributo) {
        if (atributo == null) return null;

        // Evitar depender de getSimpleName()
        if (atributo instanceof AlgoritmoConsensoMayoriaSimple)    return COD_MS;
        if (atributo instanceof AlgoritmoConsensoMayoriaAbsoluta)  return COD_MA;
        if (atributo instanceof AlgoritmoConsensoMultiplesMenciones) return COD_MM;

        // Fallback defensivo
        throw new IllegalArgumentException("AlgoritmoConsenso desconocido: " + atributo.getClass());
    }

    @Override
    public IAlgoritmoConsenso convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        String code = dbData.trim().toUpperCase();

        switch (code) {
            case COD_MS: return new AlgoritmoConsensoMayoriaSimple();
            case COD_MA: return new AlgoritmoConsensoMayoriaAbsoluta();
            case COD_MM: return new AlgoritmoConsensoMultiplesMenciones();
            default:
                // Podés retornar null, pero es mejor fallar explícito para detectar datos inválidos:
                throw new IllegalArgumentException("Código de algoritmo inválido en BD: " + dbData);
        }
    }
}

