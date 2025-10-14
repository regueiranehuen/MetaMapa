package modulos.Front.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {
    @GetMapping("/login")
    public String login(){
        return "login";
    }

    // Del post mapping se encarga spring security. Sabe cómo usar todas estas cosas por definir el comportamiento en un service definido

    // Las sesiones se manejan del lado del servidor.
    // El cliente tiene una cookie que va a tener un id de sesion
    // En cada request que haga ese cliente (user a traves del cliente), va a venir con esa cookie que va a tener un id de sesion
    // Con el id de sesion, buscamos si existe un id de sesion asi y asi podemos identificar al usuario
    // Al hacer logout, la sesion se destruye y se invalida la cookie

}