package com.tpagrupo17.Metamapa.entities.DbEstatica;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import com.tpagrupo17.Metamapa.entities.DbMain.Hecho;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "hecho_estatica")
@SuperBuilder(toBuilder = true)
@Setter
@Getter
@AllArgsConstructor
public class HechoEstatica extends Hecho {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hecho_dataset",
            joinColumns = @JoinColumn(name = "hecho_id"),
            inverseJoinColumns = @JoinColumn(name = "dataset_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_hecho_dataset", columnNames = {"hecho_id","dataset_id"})
    )
    private List<Dataset> datasets;

    public HechoEstatica(){
        this.datasets = new ArrayList<>();
    }
}
