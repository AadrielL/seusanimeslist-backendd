package com.seusanimes.security; 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public JwtRequestFilter(CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        boolean shouldNotFilter = path.startsWith("/api/auth");
        //logger.debug("JwtRequestFilter: shouldNotFilter para {}: {}", path, shouldNotFilter); // Pode ser ruidoso, descomente se precisar depurar filtros não-autenticados
        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        logger.info("JwtRequestFilter: Recebendo requisição para URI: {}", request.getRequestURI()); // Alterei para info, pois é uma log importante

        // CORS Preflight requests (OPTIONS method) should be allowed without JWT
        if (request.getMethod().equals("OPTIONS")) {
            logger.info("JwtRequestFilter: Tratando requisição OPTIONS. Permitindo CORS preflight.");
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return; // Importante parar o processamento aqui
        }

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            logger.info("JwtRequestFilter: JWT extraído: {}", jwtToken); // Alterei para info
            try {
                username = jwtUtil.extractUsername(jwtToken);
                logger.info("JwtRequestFilter: Username extraído do JWT: {}", username); // Alterei para info
            } catch (Exception e) {
                logger.error("JwtRequestFilter: Erro ao extrair username ou token inválido/expirado para URI {}: {}", request.getRequestURI(), e.getMessage());
                // Em caso de token inválido/expirado, retorna 401 e interrompe a cadeia de filtros
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Token JWT inválido ou expirado. " + e.getMessage() + "\"}");
                return; // INTERROMPE O FLUXO AQUI
            }
        } else {
            logger.warn("JwtRequestFilter: JWT Token não começa com Bearer String ou está ausente para URI: {}", request.getRequestURI());
            // Se não há token e a URI não é '/api/auth', o Spring Security
            // naturalmente gerará um 403 ou 401 mais adiante se a rota for protegida.
            // Não precisamos interromper o filtro aqui, a menos que queiramos
            // uma mensagem de erro customizada para "token ausente".
            // Por enquanto, vamos deixar que o fluxo do Spring Security lide com isso.
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("JwtRequestFilter: Tentando carregar UserDetails para username: {}", username); // Alterei para info
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            logger.info("JwtRequestFilter: UserDetails carregado: {}", userDetails.getUsername()); // Alterei para info

            // <<<<<<<<<<<<<<<< SUA CORREÇÃO APLICADA AQUI: Passando userDetails.getUsername() para validateToken >>>>>>>>>>>>>>>>>>
           if (jwtUtil.validateToken(jwtToken, userDetails.getUsername())) { // CORREÇÃO AQUI // <<-- CORRIGIDO PARA PASSAR UserDetails inteiro ou usar getUsername()
                logger.info("JwtRequestFilter: Token VALIDADO com sucesso para usuário: {}", username); // Alterei para info

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.info("JwtRequestFilter: Usuário {} autenticado no SecurityContext.", username); // Log de sucesso

            } else {
                logger.warn("JwtRequestFilter: Token JWT inválido para usuário: {} ou validação falhou.", username);
                // Se a validação do token falhar, você pode optar por retornar 401 imediatamente.
                // O JwtAuthenticationEntryPoint também pegaria isso, mas um retorno explícito pode dar mais controle.
                // Por agora, vou deixar que o JwtAuthenticationEntryPoint lide com isso.
            }
        } else if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.info("JwtRequestFilter: Usuário {} já autenticado no SecurityContext. Continuando...", username); // Alterei para info
        } else if (username == null && jwtToken != null) {
            // Este caso pode acontecer se o token foi extraído, mas o username veio nulo (token malformado)
            logger.warn("JwtRequestFilter: Token presente, mas username é nulo. Token inválido?");
        } else {
            logger.info("JwtRequestFilter: Sem token JWT ou username para processar."); // Para requisições públicas
        }


        filterChain.doFilter(request, response);
    }
}