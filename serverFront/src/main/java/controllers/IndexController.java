package controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Index");
        return "index";
    }

    @GetMapping
    public String notFound(Model model) {
        model.addAttribute("titulo", "No encontrado");
        return "404";
    }
}
