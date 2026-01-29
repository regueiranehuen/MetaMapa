package com.tpagrupo17.Metamapa.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import java.security.Key;
import java.util.Date;
import java.util.List;

public class JwtClaimExtractor {

    public static String getUsernameFromToken(Jwt token){
        Claims  claims = (Claims) token.getBody();
        return claims.getSubject();
    }

    public static String getRefreshToken(Jwt token){
        Claims claims = (Claims) token.getBody();
        return claims.get("refreshToken").toString();
    }

    public static String getToken(Jwt token){
        Claims claims = (Claims) token.getBody();
        return claims.get("token").toString();
    }

    // ⚠️ Esta clave debe ser la misma usada para firmar el token y debe ser SECRETA.
    private final String SECRET_BASE64 = "TuClaveSecretaMuyLargaYSegura_Base64EncodedString";

    private Key getSigningKey() {
        // Convierte el String Base64 a un objeto Key seguro
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Decodifica, verifica y extrae los claims (cuerpo) del JWT.
     * @param token El JWT en formato String (sin el prefijo "Bearer ").
     * @return El objeto Claims que contiene todos los parámetros del payload.
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // Verifica la firma del token
                    .build()
                    .parseClaimsJws(token) // Si falla la firma o la fecha, lanza una excepción
                    .getBody(); // Devuelve el payload (Claims)

        } catch (Exception e) {
            // Aquí se capturan errores de: token expirado, firma inválida, etc.
            throw new RuntimeException("Error al validar o decodificar el token JWT: " + e.getMessage());
        }
    }

    // --- Uso de la función para acceder a los parámetros ---
    public void accederAParametros(String jwtToken) {
        Claims claims = extractClaims(jwtToken);

        // 1. Claims estándar (Registered Claims)
        String subject = claims.getSubject(); // 'sub' (Ej: ID de usuario)
        Date expiration = claims.getExpiration(); // 'exp' (Ej: Fecha de caducidad)

        
        

        // 2. Claims personalizados

        // Acceder a un String (Ej: nombre del rol)
        String rol = claims.get("rol_principal", String.class);
        

        // Acceder a una lista de Strings (Ej: roles/authorities)
        List<String> rolesList = claims.get("roles", List.class);
        
    }
}
