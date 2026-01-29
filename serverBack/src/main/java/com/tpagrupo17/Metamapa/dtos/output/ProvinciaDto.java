package com.tpagrupo17.Metamapa.dtos.output;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProvinciaDto {
    private String provincia;
    private Long id;
}
