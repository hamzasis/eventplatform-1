package com.springprjt.springboot.controller;

import com.springprjt.springboot.exception.UnauthorizedException;
import com.springprjt.springboot.model.User;
import com.springprjt.springboot.service.UserService;
import com.springprjt.springboot.util.JWTUtil;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JWTUtil jwtUtil;

 // User Signup
    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@RequestBody User user) {
        System.out.println("Received signup request for user: " + user.getUsername());
        User createdUser = userService.signUp(user);
        System.out.println("User created with username: " + createdUser.getUsername());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

 // User Login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        boolean isAuthenticated = userService.login(user.getUsername(), user.getPassword());

        if (isAuthenticated) {
        	User loggedInUser = userService.getUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(user.getUsername(),loggedInUser.getRole().name());

            System.out.println("Generated JWT Token: " + token);

            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
    
 // Endpoint: Retrieve all users (only accessible for ADMIN role)
    @GetMapping("/allusers")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
    	try {
            System.out.println("Authorization Header: " + token);
            
            String extractedToken = token.substring(7);  
            System.out.println("Extracted Token: " + extractedToken);
            
            jwtUtil.printTokenContent(extractedToken);

            String role = jwtUtil.extractRole(extractedToken);
            System.out.println("Role extracted from token: " + role);

            if ("ADMIN".equalsIgnoreCase(role)) {
                System.out.println("Access Granted: ADMIN Role");
                List<User> users = userService.getAllUsers();
                return ResponseEntity.ok(users);
            } else {
                System.out.println("Access Denied: Role is " + role);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to access this resource.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or malformed token.");
        }
    }
    
    @GetMapping("/{username}")
    public User getUserByUsername(@RequestHeader("Authorization") String token, 
                                  @PathVariable("username") String username) {

        String jwtToken = token.substring(7);  
        
        String usernameFromToken = jwtUtil.extractUsername(jwtToken);
        if (usernameFromToken == null || !jwtUtil.validateToken(jwtToken, usernameFromToken)) {
            throw new RuntimeException("Invalid or expired token");
        }

        User user = userService.getUserByUsername(username);
        
        if (user != null) {
            return user; 
        } else {
            throw new RuntimeException("User not found!");  
        }
    }
}
