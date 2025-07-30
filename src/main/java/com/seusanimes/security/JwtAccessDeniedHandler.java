package com.seusanimes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date; // Adicione esta importação
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Log para debug NO TERMINAL DO BACKEND
        System.out.println("JwtAccessDeniedHandler: Acesso negado para URI: " + request.getRequestURI());
        System.out.println("JwtAccessDeniedHandler: Mensagem de erro: " + accessDeniedException.getMessage());
        System.out.println("JwtAccessDeniedHandler: Tipo de exceção: " + accessDeniedException.getClass().getName());

        // Define o status HTTP como 403 Forbidden
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        // Cria um corpo de resposta JSON customizado que aparecerá no navegador
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", new Date());
        errorDetails.put("status", HttpStatus.FORBIDDEN.value());
        errorDetails.put("error", "Forbidden");
        errorDetails.put("message", "Você não tem permissão para acessar este recurso. Detalhe: " + accessDeniedException.getMessage());
        errorDetails.put("path", request.getRequestURI());

        // Converte o mapa para JSON e escreve na resposta
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), errorDetails);
    }
}