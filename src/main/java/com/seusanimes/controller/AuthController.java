package com.seusanimes.controller;

import com.seusanimes.model.User;
import com.seusanimes.service.UserService;
import com.seusanimes.security.JwtUtil; // Importar o JwtUtil
import com.seusanimes.security.CustomUserDetailsService; // Importar o CustomUserDetailsService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Importar AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException; // Para erros de credenciais
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Para o objeto de autenticação
import org.springframework.security.core.userdetails.UserDetails; // Para obter UserDetails após autenticação
import org.springframework.web.bind.annotation.*;
import com.seusanimes.dto.JwtResponse; // Certifique-se que o pacote esteja correto
import java.util.Optional; // Note: Optional is imported but not used in the provided snippet. Keep it if used elsewhere.

@CrossOrigin(origins = "http://localhost:5173") // Ajuste isso para o domínio do seu frontend
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager; // Injetar AuthenticationManager
    private final JwtUtil jwtUtil; // Injetar JwtUtil
    private final CustomUserDetailsService userDetailsService; // Injetar CustomUserDetailsService

    @Autowired
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager, // Adicionar ao construtor
                          JwtUtil jwtUtil, // Adicionar ao construtor
                          CustomUserDetailsService userDetailsService) { // Adicionar ao construtor
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // Método de registro - Mantido como você o enviou
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isBlank() ||
            user.getEmail() == null || user.getEmail().isBlank() ||
            user.getPassword() == null || user.getPassword().isBlank()) {
            return new ResponseEntity<>("Username, email e senha são obrigatórios.", HttpStatus.BAD_REQUEST);
        }

        User registeredUser = userService.registerUser(user);

        if (registeredUser == null) {
            return new ResponseEntity<>("Username ou email já cadastrado.", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>("Usuário registrado com sucesso!", HttpStatus.CREATED);
    }


    /**
     * Endpoint para fazer login de um usuário e retornar um JWT.
     * Recebe um objeto com username e password no corpo da requisição (JSON).
     * Retorna o token JWT em caso de sucesso.
     */
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User authenticationRequest) throws Exception {
        try {
            // Tenta autenticar o usuário usando o AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Credenciais inválidas (username ou senha incorretos)
            return new ResponseEntity<>("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }

        // Se a autenticação foi bem-sucedida, carrega os detalhes do usuário
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        // Gera o token JWT
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        // CORREÇÃO APLICADA AQUI:
        // Retorna o token JWT empacotado em um objeto JwtResponse
        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}

// Certifique-se de que sua classe JwtResponse.java exista com este conteúdo (se estiver em um pacote diferente, ajuste o import no AuthController)
/*
// com.seusanimes.controller/JwtResponse.java (ou com.seusanimes.dto/JwtResponse.java)
package com.seusanimes.controller; // Ou o pacote do seu DTO

public class JwtResponse {
    private String token;

    public JwtResponse(String token) {
        this.token = token;
    }

    // Getters e Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
*/