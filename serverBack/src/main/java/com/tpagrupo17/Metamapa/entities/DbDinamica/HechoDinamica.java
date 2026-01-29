package com.tpagrupo17.Metamapa.entities.DbDinamica;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.experimental.SuperBuilder;
import com.tpagrupo17.Metamapa.entities.DbMain.Hecho;

@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "hecho_dinamica")
public class HechoDinamica extends Hecho {
    public HechoDinamica() {
    }
}


