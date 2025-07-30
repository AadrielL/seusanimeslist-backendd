    package com.seusanimes.security; // Ou com.seusanimes.util

    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.security.Keys;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Component;

    import java.security.Key;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.function.Function;

@Component // Indica que esta classe é um componente Spring
public class JwtUtil {

    // Chave secreta para assinar e validar o token.
    // É crucial que esta chave seja FORTE e não seja exposta!
    // Puxe de variáveis de ambiente em produção.
    @Value("${jwt.secret:umaChaveSuperSecretaQueNinguemVaiAdivinharParaAssinarMeuJWTComPeloMenos256Bits}") // Use uma chave forte
    private String secret;

    // Tempo de expiração do token em milissegundos (ex: 10 horas)
    @Value("${jwt.expiration:36000000}") // 10 horas em ms
    private long expiration;

    // Converte a string secreta em um objeto Key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Extrair o username do token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrair a data de expiração do token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extrair uma "claim" específica (parte da carga útil do token)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrair todas as "claims" do token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Verificar se o token expirou
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Gerar um token JWT para um determinado username
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        // Você pode adicionar claims personalizadas aqui, como roles (papéis do usuário)
        return createToken(claims, username);
    }

    // Lógica para criar o token
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims) // Carga útil do token
                .setSubject(subject) // O assunto (geralmente o username ou ID do usuário)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Data de emissão
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Data de expiração
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Assinatura com a chave secreta e algoritmo
                .compact(); // Constrói e compacta o token
    }

    // Validar o token
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}