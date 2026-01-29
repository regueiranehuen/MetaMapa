package com.tpagrupo17.Metamapa.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JsonCleaner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Path carpeta = Path.of(".idea/httpRequests");
        if (Files.exists(carpeta) && Files.isDirectory(carpeta)) {
            File[] archivos = carpeta.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

            if (archivos != null) {
                for (File archivo : archivos) {
                    archivo.delete();
                }
            }
        }
    }
}
