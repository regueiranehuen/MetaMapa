package com.tpagrupo17.Metamapa.entities.DbProxy;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.experimental.SuperBuilder;
import com.tpagrupo17.Metamapa.entities.DbMain.Hecho;

@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "hecho_proxy")
public class HechoProxy extends Hecho {
    public HechoProxy() {
    }
}
