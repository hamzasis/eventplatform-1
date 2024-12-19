package com.springprjt.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Encrypt passwords
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .requestMatchers("/api/users/login", "/api/users/signup", "/api/users/{username}", "/api/event/*","/api/registration/**","/api/registration/register/{eventId}","/api/registration/update/{registrationId}","/api/registrationCount/{eventId}","/event/registrationCount/{eventId}")
                    .permitAll()  // Public endpoints
                .requestMatchers("/api/users/allusers")
                    .hasAnyAuthority("ADMIN", "ORGANIZER")
                    
                .anyRequest().authenticated()
            .and()
            .formLogin().disable(); 
        return http.build();
    }

}
