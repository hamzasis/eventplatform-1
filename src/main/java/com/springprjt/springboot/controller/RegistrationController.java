package com.springprjt.springboot.controller;

import com.springprjt.springboot.model.Registration;
import com.springprjt.springboot.model.User;
import com.springprjt.springboot.service.RegistrationService;
import com.springprjt.springboot.service.UserService;
import com.springprjt.springboot.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private JWTUtil jwtUtil;
    
    @PostMapping("/register/{eventId}")
    public ResponseEntity<String> registerForEvent(@RequestHeader("Authorization") String token, @PathVariable Long eventId) {
        String username = jwtUtil.extractUsername(token.substring(7));
        String role = jwtUtil.extractRole(token.substring(7));

        if ("ATTENDEE".equals(role)) {
            User user = userService.getUserByUsername(username);
            String result = registrationService.registerUserForEvent(eventId, user.getId());
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Attendees can register.");
        }
    }

    @PutMapping("/update/{registrationId}")
    public ResponseEntity<String> updateRegistrationStatus(@RequestHeader("Authorization") String token, 
                                                            @PathVariable Long registrationId, 
                                                            @RequestParam String status) {
        String role = jwtUtil.extractRole(token.substring(7));
        if ("ORGANIZER".equals(role) || "ADMIN".equals(role)) {
            String result = registrationService.updateRegistrationStatus(registrationId, status);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only Organizers or Admins can update registration status.");
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registration>> getRegistrationsForEvent(@RequestHeader("Authorization") String token, 
                                                                         @PathVariable Long eventId) {
        String role = jwtUtil.extractRole(token.substring(7));
        if ("ORGANIZER".equals(role) || "ADMIN".equals(role)) {
            List<Registration> registrations = registrationService.getRegistrationsForEvent(eventId);
            return ResponseEntity.ok(registrations);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/event/registrationCount/{eventId}")
    public ResponseEntity<Long> getRegistrationCountForEvent(@RequestHeader("Authorization") String token, 
                                                              @PathVariable Long eventId) {
        String role = jwtUtil.extractRole(token.substring(7));
        if ("ADMIN".equals(role)) {
            long count = registrationService.countRegistrationsForEvent(eventId);
            return ResponseEntity.ok(count);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
}
