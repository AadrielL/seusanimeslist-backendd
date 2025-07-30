package com.seusanimes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections; // Para Collections.singletonList
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // <<-- IMPORTANTE: Importe UserDetails

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Data // Esta anotação do Lombok já gera getters, setters, construtores, etc.
public class User implements UserDetails { // <<-- MUITO IMPORTANTE: Implemente UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // Remova `nullable = false` se `updatedAt` puder ser nulo no início
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now(); // Initialize updatedAt on creation as well
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- MÉTODOS DA INTERFACE UserDetails (Adicione estes métodos!) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por enquanto, podemos retornar uma role padrão ou uma lista vazia.
        // O "ROLE_" é um prefixo que o Spring Security geralmente espera para roles.
        // Se você tiver um campo de role na sua entidade User, use-o aqui.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password; // Retorna a senha já codificada
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Retorne true se sua conta nunca expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Retorne true se sua conta nunca é bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Retorne true se as credenciais nunca expiram
    }

    @Override
    public boolean isEnabled() {
        return true; // Retorne true se o usuário está sempre habilitado
    }

    // Você já tem getters/setters via @Data do Lombok.
    // Se precisar de construtores específicos para criação, adicione-os:
    // public User(String username, String email, String password) {
    //     this.username = username;
    //     this.email = email;
    //     this.password = password;
    // }
}