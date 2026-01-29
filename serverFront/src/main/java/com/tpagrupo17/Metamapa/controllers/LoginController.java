package com.tpagrupo17.Metamapa.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class LoginController {

    @GetMapping(("/login"))
    public String login(){
        return "login";
    }
}
