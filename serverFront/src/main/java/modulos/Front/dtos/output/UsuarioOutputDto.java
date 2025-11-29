package modulos.Front.dtos.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import modulos.Front.usuario.Rol;

@Data
@AllArgsConstructor
public class UsuarioOutputDto {
    private Long id;
    private String nombreDeUsuario;
    private String nombre;
    private String apellido;
    private Integer edad;
    private Integer cantHechosSubidos;
    private Rol rol;
    public UsuarioOutputDto(){}
}
