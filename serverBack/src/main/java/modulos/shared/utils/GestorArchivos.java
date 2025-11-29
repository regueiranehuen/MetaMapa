package modulos.shared.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

import java.util.UUID;

public class GestorArchivos {

    // Carpeta de uploads = <directorio donde corre el jar>/uploads
    private static final String UPLOAD_DIR = "uploads/";

    public static String guardarArchivo(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        System.out.println("UPLOAD DIR ABSOLUTO: " + uploadPath);

        if (Files.notExists(uploadPath)) {
            System.out.println("NO EXISTE uploads, la creo");
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // URL pública relativa
        return "/uploads/" + fileName;
    }

    public static boolean eliminarArchivo(String rutaArchivo) {
        try {
            Path path = Paths.get(rutaArchivo);

            // Verifica si existe
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("Archivo eliminado: " + rutaArchivo);
                return true;
            } else {
                System.out.println("El archivo no existe: " + rutaArchivo);
                return false;
            }

        } catch (IOException e) {
            System.err.println("Error al eliminar el archivo: " + e.getMessage());
            return false;
        }
    }

}
