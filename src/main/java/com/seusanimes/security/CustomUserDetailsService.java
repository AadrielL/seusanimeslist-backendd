package com.seusanimes.security; // Ou em outro pacote de segurança que você tenha criado

import com.seusanimes.model.User;
import com.seusanimes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Remova esta importação, pois você não usará mais ArrayList diretamente aqui
// import java.util.ArrayList;

@Service // Marca a classe como um serviço Spring
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tenta encontrar o usuário no seu banco de dados usando o UserRepository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // 2. RETORNE A PRÓPRIA INSTÂNCIA DO SEU OBJETO USER,
        // pois ele já implementa UserDetails e tem o método getAuthorities()
        System.out.println("CustomUserDetailsService: Usuário '" + user.getUsername() + "' carregado. Roles: " + user.getAuthorities()); // DEBUG
        return user;
    }
}