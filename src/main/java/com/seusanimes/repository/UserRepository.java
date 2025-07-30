package com.seusanimes.repository; // Ajuste o pacote

import com.seusanimes.model.User; // Importa a sua entidade User
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Usado para métodos que podem não encontrar um resultado

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Método personalizado para buscar um usuário pelo username
    Optional<User> findByUsername(String username);

    // Método personalizado para buscar um usuário pelo email
    Optional<User> findByEmail(String email);

    // Você pode adicionar mais métodos personalizados conforme a necessidade,
    // por exemplo, para verificar se um username ou email já existe antes de registrar.
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}