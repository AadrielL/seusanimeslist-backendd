package com.seusanimes.controller;

import com.seusanimes.model.User;
import com.seusanimes.service.UserService;
import com.seusanimes.security.JwtUtil;
import com.seusanimes.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.seusanimes.dto.JwtResponse;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

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

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody User authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>("Credenciais inválidas.", HttpStatus.UNAUTHORIZED);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}