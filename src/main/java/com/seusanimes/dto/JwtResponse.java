// src/main/java/com/seusanimes/dto/JwtResponse.java
package com.seusanimes.dto; // Garanta que este pacote é o mesmo do UserAnimeResponse

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