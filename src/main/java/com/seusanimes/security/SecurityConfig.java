package com.seusanimes.security; // Define o pacote onde a classe está localizada

import org.springframework.beans.factory.annotation.Autowired; // Para injeção de dependências
import org.springframework.context.annotation.Bean; // Para declarar métodos que produzem beans do Spring
import org.springframework.context.annotation.Configuration; // Indica que esta classe é uma fonte de definições de beans
import org.springframework.http.HttpMethod; // Importação para HttpMethod
import org.springframework.security.authentication.AuthenticationManager; // Gerencia o processo de autenticação
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Provedor de autenticação para usuários de banco de dados
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Para obter o AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Configura a segurança baseada em web
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Habilita a integração com Spring Security
import org.springframework.security.config.http.SessionCreationPolicy; // Define a política de criação de sessão
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Codificador de senha
import org.springframework.security.crypto.password.PasswordEncoder; // Interface para codificadores de senha
import org.springframework.security.web.SecurityFilterChain; // A cadeia de filtros de segurança HTTP
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Um filtro de autenticação padrão

// Importações para CORS (Cross-Origin Resource Sharing)
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays; // Utilitário para arrays

@Configuration // Indica ao Spring que esta é uma classe de configuração e deve ser processada para beans
@EnableWebSecurity // Habilita as funcionalidades de segurança web do Spring Security
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService; // Serviço para carregar detalhes do usuário
    private final JwtRequestFilter jwtRequestFilter; // Seu filtro JWT personalizado
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler; // Handler para acesso negado (403)
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // Handler para entrada de autenticação (401)

    // Construtor para injeção de dependências (o Spring injeta as instâncias necessárias)
    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtRequestFilter jwtRequestFilter,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     * Define a cadeia de filtros de segurança HTTP.
     * Este é o método central onde as regras de segurança são configuradas.
     * @param http O objeto HttpSecurity para configurar a segurança.
     * @return A cadeia de filtros de segurança configurada.
     * @throws Exception Se ocorrer um erro durante a configuração.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Desabilita a proteção CSRF (Cross-Site Request Forgery).
            // É comum desabilitar em APIs RESTful que usam JWT,
            // pois JWTs são stateless e a proteção CSRF baseada em sessão não se aplica.
            .csrf(csrf -> csrf.disable())

            // 2. Configura o CORS (Cross-Origin Resource Sharing).
            // Permite que seu frontend (localhost:5173) faça requisições ao backend (localhost:8081).
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. Define as regras de autorização para requisições HTTP.
            .authorizeHttpRequests(authorize -> authorize
                // Permite acesso público (sem autenticação) a todos os endpoints sob /api/auth/**
                // Isso inclui rotas como login e registro.
                .requestMatchers("/api/auth/**").permitAll()
                
                // <<< INÍCIO DAS REGRAS CORRIGIDAS/AJUSTADAS PARA AS BUSCAS >>>
                // A regra abaixo cobre:
                // - GET /api/animes (para listar todos)
                // - GET /api/animes?titulo={titulo} (para busca por título)
                // - GET /api/animes?ano={ano} (para busca por ano)
                .requestMatchers(HttpMethod.GET, "/api/animes").authenticated()

                // Regras para endpoints específicos que talvez você use no frontend:
                // Se você tiver um endpoint /api/animes/{id} para buscar por ID, ele deve ser autenticado:
                .requestMatchers(HttpMethod.GET, "/api/animes/{id}").authenticated()
                // Se você tiver um endpoint /api/animes/search?q={query} para busca genérica:
                .requestMatchers(HttpMethod.GET, "/api/animes/search").authenticated()
                // Se você tiver um endpoint /api/animes/genre/{genre} para busca por gênero:
                .requestMatchers(HttpMethod.GET, "/api/animes/genre/{genre}").authenticated()

                // Regras para operações de importação na API externa (se essas operações forem protegidas)
                // Note que estas são POST para o /api/animes, então precisam estar autenticadas
                .requestMatchers(HttpMethod.POST, "/api/animes/buscar-e-salvar").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/animes/buscar-e-salvar-por-ano").authenticated()
                // Se a criação manual de animes for permitida e precisa de autenticação
                .requestMatchers(HttpMethod.POST, "/api/animes").authenticated()
                // <<< FIM DAS REGRAS CORRIGIDAS/AJUSTADAS PARA AS BUSCAS >>>

                // Regras para os endpoints de usuário (Minha Lista), que já estão funcionando:
                .requestMatchers(HttpMethod.POST, "/api/user-animes").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/user-animes").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/user-animes/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/user-animes/**").authenticated()

                // Qualquer outra requisição que não foi explicitamente permitida acima,
                // DEVE ser autenticada. Ou seja, o usuário precisa ter um JWT válido e ser reconhecido.
                .anyRequest().authenticated()
            )

            // 4. Configura o gerenciamento de sessão.
            .sessionManagement(session -> session
                // Define a política de criação de sessão como STATELESS.
                // Isso significa que o Spring Security NÃO criará ou usará sessões HTTP.
                // Cada requisição deve conter todas as informações necessárias para autenticação (o JWT).
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 5. Configura o tratamento de exceções de segurança.
            .exceptionHandling(exceptions -> exceptions
                // Este handler será invocado quando um usuário NÃO AUTENTICADO (ou com token inválido)
                // tentar acessar um recurso protegido (resultando em um 401 Unauthorized ou, em alguns casos, 403).
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                // Este handler será invocado quando um usuário AUTENTICADO tentar acessar
                // um recurso para o qual não tem permissão (resultando em um 403 Forbidden).
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )

            // 6. Removida a linha .anonymous(anonymous -> anonymous.disable()) como na versão anterior.
            // Esta linha foi removida, pois era uma provável causa do 401 Unauthorized inesperado
            // em requisições autenticadas, interferindo no contexto de segurança.

            // 7. Adiciona o provedor de autenticação personalizado.
            // Indica ao Spring Security qual provedor será usado para autenticar os usuários.
            .authenticationProvider(authenticationProvider())

            // 8. Adiciona o filtro JWT personalizado ANTES do filtro padrão de autenticação de nome de usuário/senha.
            // Isso garante que seu JWTRequestFilter intercepte e processe o JWT primeiro.
            // Se o JWT for válido, o usuário será autenticado, e os filtros subsequentes podem pular a autenticação tradicional.
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // Constrói e retorna a cadeia de filtros de segurança configurada.
        return http.build();
    }

    /**
     * Configura o provedor de autenticação que usará seu CustomUserDetailsService
     * e o codificador de senhas.
     * @return Um DaoAuthenticationProvider configurado.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Define o serviço que carregará os detalhes do usuário (do banco de dados).
        authProvider.setUserDetailsService(userDetailsService);
        // Define o codificador de senhas a ser usado para verificar a senha fornecida com a senha armazenada.
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expose o AuthenticationManager como um bean.
     * O AuthenticationManager é usado para realizar o processo de autenticação,
     * por exemplo, para autenticar um usuário no endpoint de login.
     * @param authConfig A configuração de autenticação.
     * @return O AuthenticationManager.
     * @throws Exception Se ocorrer um erro.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Define o codificador de senhas (BCryptPasswordEncoder) como um bean.
     * É crucial usar um PasswordEncoder para armazenar e comparar senhas de forma segura.
     * @return Uma instância de BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura a política CORS para a aplicação.
     * Isso permite que requisições de origens específicas acessem seu backend.
     * @return Um CorsConfigurationSource.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite requisições do seu frontend rodando em localhost:5173.
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // Permite os métodos HTTP comuns.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permite os cabeçalhos HTTP necessários (incluindo Authorization para JWTs).
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // Permite que credenciais (como cookies ou cabeçalhos de autenticação) sejam incluídas nas requisições cross-origin.
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuração CORS a todos os caminhos (/**).
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}