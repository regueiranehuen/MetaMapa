package com.tpagrupo17.Metamapa.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.tpagrupo17.Metamapa.entities.DbMain.hechoRef.AtributosHechoMemoria;
import com.tpagrupo17.Metamapa.entities.DbEstatica.Dataset;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class HechoMemoria {
    private Long id;

    private Boolean activo;

    private Long usuario_id;

    private AtributosHechoMemoria atributosHecho;


    private List<Dataset> datasets;

    public HechoMemoria(Long id, Boolean activo, Long usuario_id) {
        this.id = id;
        this.activo = activo;
        this.usuario_id = usuario_id;
        this.atributosHecho = new AtributosHechoMemoria();
    }

    public HechoMemoria(){
        this.atributosHecho = new AtributosHechoMemoria();
        this.datasets = new ArrayList<>();
    }
}
