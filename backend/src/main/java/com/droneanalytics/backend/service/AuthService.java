package com.droneanalytics.backend.service;

import com.droneanalytics.backend.entity.User;
import com.droneanalytics.backend.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AuthService {
    
    // Временное хранилище пользователей (позже заменим на БД)
    private final List<User> users = Arrays.asList(
        new User("admin", "admin123", "ADMIN"),
        new User("analyst", "analyst123", "ANALYST")
    );

    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String authenticate(String username, String password) {
        System.out.println("=== AUTH DEBUG ===");
        System.out.println("📧 Username received: '" + username + "'");
        System.out.println("🔑 Password received: '" + password + "'");
        System.out.println("Available users:");
        users.forEach(user -> System.out.println(" - " + user.getUsername() + "/" + user.getPassword()));
        
        String result = users.stream()
            .filter(user -> {
                boolean usernameMatch = user.getUsername().equals(username);
                boolean passwordMatch = user.getPassword().equals(password);
                System.out.println("User '" + user.getUsername() + "' match: " + usernameMatch + ", password: " + passwordMatch);
                return usernameMatch && passwordMatch;
            })
            .findFirst()
            .map(user -> {
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                System.out.println("✅ SUCCESS: Token generated for " + username);
                return token;
            })
            .orElse(null);
            
        if (result == null) {
            System.out.println("❌ FAILED: No matching user found");
        }
        System.out.println("=== END AUTH DEBUG ===");
        return result;
    }

    public User getUserByUsername(String username) {
        return users.stream()
            .filter(user -> user.getUsername().equals(username))
            .findFirst()
            .orElse(null);
    }
}