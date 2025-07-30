package com.seusanimes.service;

 
import com.seusanimes.model.User;
import com.seusanimes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder; // Vamos precisar disso para criptografar senhas
import java.util.Optional;

@Service // Indica que esta classe é um componente de serviço Spring
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Usaremos para criptografar senhas

    // Injeção de dependência via construtor (forma recomendada)
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * @param user O objeto User com os dados para registro (username, email, password).
     * @return O User salvo, ou null se o username/email já existirem.
     */
    public User registerUser(User user) {
        // 1. Validação: Verificar se o username ou email já existem
        if (userRepository.existsByUsername(user.getUsername())) {
            // Poderíamos lançar uma exceção personalizada aqui
            System.out.println("Erro: Username já existe.");
            return null; // Ou lançar uma exceção como new UsernameAlreadyExistsException()
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            System.out.println("Erro: Email já existe.");
            return null; // Ou lançar uma exceção como new EmailAlreadyExistsException()
        }

        // 2. Criptografar a senha antes de salvar
        // IMPORTANTÍSSIMO: NUNCA salve senhas em texto puro!
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. Salvar o usuário no banco de dados
        return userRepository.save(user);
    }

    /**
     * Tenta autenticar um usuário.
     * (Esta é uma versão simplificada. Em um sistema real, Spring Security faria a maior parte disso).
     *
     * @param username O username do usuário.
     * @param rawPassword A senha em texto puro fornecida pelo usuário.
     * @return O User autenticado, ou Optional.empty() se as credenciais forem inválidas.
     */
    public Optional<User> authenticateUser(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Comparar a senha bruta com a senha criptografada no banco
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return Optional.of(user); // Credenciais válidas
            }
        }
        return Optional.empty(); // Usuário não encontrado ou senha inválida
    }

    /**
     * Busca um usuário pelo ID.
     *
     * @param id O ID do usuário.
     * @return Um Optional contendo o User, se encontrado.
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Busca um usuário pelo username.
     *
     * @param username O username do usuário.
     * @return Um Optional contendo o User, se encontrado.
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Você pode adicionar mais métodos aqui conforme a necessidade:
    // - Atualizar perfil do usuário
    // - Resetar senha
    // - Deletar usuário (com cuidado!)
}