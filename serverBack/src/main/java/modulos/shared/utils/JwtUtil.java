package modulos.shared.utils;

import lombok.Getter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import modulos.agregacion.entities.DbMain.usuario.Rol;

import java.security.Key;
import java.util.Date;


public class JwtUtil {
    @Getter
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long ACCESS_TOKEN_VALIDITY = 8 * 60 * 60 * 1000; // 8 horas
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 días

    public static String generarAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("gestion-alumnos-server")
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(key)
                .compact();
    }

    public static String generarRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("gestion-alumnos-server")
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .claim("type", "refresh") // diferenciamos refresh del access
                .signWith(key)
                .compact();
    }

    // Podemos descifrar el código -> es válido
    public static Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
