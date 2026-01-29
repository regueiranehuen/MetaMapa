package com.tpagrupo17.Metamapa.entities.fuentes.Requests;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
