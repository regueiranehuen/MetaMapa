package com.tpagrupo17.Metamapa.dtos.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaisDto {
    private String pais;
    private Long id;
}
