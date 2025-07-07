package com.eaglebank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private String userId;
    private String email;
    
    public AuthResponse(String token, String userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
    }
}

