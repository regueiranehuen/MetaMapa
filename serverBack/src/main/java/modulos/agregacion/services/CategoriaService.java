package modulos.agregacion.services;

import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import modulos.JwtClaimExtractor;
import modulos.agregacion.entities.DbMain.Categoria;
import modulos.agregacion.entities.DbMain.Sinonimo;
import modulos.agregacion.entities.DbMain.usuario.Rol;
import modulos.agregacion.entities.DbMain.usuario.Usuario;
import modulos.agregacion.repositories.DbMain.ICategoriaRepository;
import modulos.agregacion.repositories.DbMain.ISinonimoRepository;
import modulos.agregacion.repositories.DbMain.IUsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CategoriaService {

    private ICategoriaRepository categoriaRepository;
    private IUsuarioRepository usuarioRepository;
    private ISinonimoRepository repoSinonimo;

    private ResponseEntity<?> checkeoAdmin(String username){

        if (username == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Usuario usuario = usuarioRepository.findByNombreDeUsuario(username).orElse(null);

        if (usuario == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el usuario");
        }
        else if (!usuario.getRol().equals(Rol.ADMINISTRADOR)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(usuario);
    }

    public ResponseEntity<?> addCategoria(String username, String categoriaStr) {

        ResponseEntity<?> rta = checkeoAdmin(username);

        if (!rta.getStatusCode().equals(HttpStatus.OK)){
            return rta;
        }
        Categoria categoria = new Categoria();
        categoria.setTitulo(categoriaStr);

        categoriaRepository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body("Se creó la categoría correctamente");
    }

    public ResponseEntity<?> addSinonimoCategoria(String username, Long idCategoria, String sinonimo_str) {

        ResponseEntity<?> respuesta = checkeoAdmin(username);

        if (!respuesta.getStatusCode().equals(HttpStatus.OK)){
            return respuesta;
        }

        Categoria categoria = categoriaRepository.findById(idCategoria).orElse(null);

        if(categoria == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","El id de la categoria no es valido"));
        }

        Sinonimo sinonimo = repoSinonimo.findByIdCategoriaAndNombre(idCategoria, sinonimo_str).orElse(null);

        if(sinonimo != null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El sinonimo ya existe");
        }

        sinonimo = new Sinonimo(sinonimo_str);

        categoria.getSinonimos().add(sinonimo);

        categoriaRepository.save(categoria);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
