package modulos.shared.dtos.output;

import lombok.Data;
import modulos.agregacion.entities.DbMain.usuario.Rol;

@Data
public class UsuarioOutputDto {
    private Long id;
    private String nombreDeUsuario;
    private String nombre;
    private String apellido;
    private Integer edad;
    private Integer cantHechosSubidos;
    private Rol rol;
}
