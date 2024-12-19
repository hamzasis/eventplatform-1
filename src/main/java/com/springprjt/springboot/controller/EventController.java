package com.springprjt.springboot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springprjt.springboot.model.Event;
import com.springprjt.springboot.service.EventService;
import com.springprjt.springboot.util.JWTUtil;

@RestController
@RequestMapping("/api/event")
public class EventController {
	@Autowired
    private EventService eventService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/newevent")
    public ResponseEntity<?> createEvent(@RequestHeader("Authorization") String token, @RequestBody Event event) {
        String username = jwtUtil.extractUsername(token.substring(7));
        String role = jwtUtil.extractRole(token.substring(7));
        System.out.println("Extracted Role from Token: " + role);


        if ("ORGANIZER".equals(role) || "ADMIN".equals(role)) {
            event.setCreatedBy(username);
            Event createdEvent = eventService.createEvent(event);
            return ResponseEntity.ok(createdEvent);
        } else {
            return ResponseEntity.status(403).body("Access denied. Only ORGANIZER or ADMIN can create events.");
        }
    }

    @GetMapping("/events")
    public ResponseEntity<?> getEvents(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("Authorization Header: " + token);

            String extractedToken = token.substring(7);
            System.out.println("Extracted Token: " + extractedToken);

            jwtUtil.printTokenContent(extractedToken);

            String role = jwtUtil.extractRole(extractedToken);
            System.out.println("Role extracted from token: " + role);

            if ("ADMIN".equalsIgnoreCase(role)) {
                System.out.println("Access Granted: ADMIN Role");
                List<Event> events = eventService.getAllEvents();
                return ResponseEntity.ok(events);
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
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            System.out.println("Authorization Header: " + token);

            String extractedToken = token.substring(7);
            System.out.println("Extracted Token: " + extractedToken);

            jwtUtil.printTokenContent(extractedToken);

            String role = jwtUtil.extractRole(extractedToken);
            System.out.println("Role extracted from token: " + role);

            if ("ADMIN".equalsIgnoreCase(role) ||
                "ORGANIZER".equalsIgnoreCase(role)) {
                
                Event event = eventService.getEventById(id);

                if (event != null) {
                    System.out.println("Event found: " + event.getTitle());
                    return ResponseEntity.ok(event);
                } else {
                    System.out.println("Event not found for ID: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Event not found.");
                }
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


    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @RequestHeader("Authorization") String token, 
            @PathVariable Long id, 
            @RequestBody Event updatedEvent) {
        try {
            String extractedToken = token.substring(7);
            String role = jwtUtil.extractRole(extractedToken);
            
            if ("ADMIN".equalsIgnoreCase(role) || "ORGANIZER".equalsIgnoreCase(role)) {
                Event event = eventService.getEventById(id);
                if (event != null) {
                    event.setTitle(updatedEvent.getTitle());
                    event.setDescription(updatedEvent.getDescription());
                    event.setLocation(updatedEvent.getLocation());
                    event.setStartDate(updatedEvent.getStartDate());
                    event.setEndDate(updatedEvent.getEndDate());
                    event.setMaxCapacity(updatedEvent.getMaxCapacity());
                    
                    eventService.saveEvent(event);  
                    return ResponseEntity.ok(event);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Event not found.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to update this event.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or malformed token.");
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @RequestHeader("Authorization") String token, 
            @PathVariable Long id) {
        try {
            String extractedToken = token.substring(7);
            String role = jwtUtil.extractRole(extractedToken);

            if ("ADMIN".equalsIgnoreCase(role)|| "ORGANIZER".equalsIgnoreCase(role)) {
                Event event = eventService.getEventById(id);
                if (event != null) {
                    eventService.deleteEvent(id); 
                    return ResponseEntity.noContent().build();

                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Event not found.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this event.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or malformed token.");
        }
    }
    
    @GetMapping("/admin/with-registration-counts")
    public ResponseEntity<Map<Event, Long>> getEventsWithRegistrationCounts(@RequestHeader("Authorization") String token) {
        String role = jwtUtil.extractRole(token.substring(7));

        if ("ADMIN".equals(role)) {
            Map<Event, Long> eventsWithCounts = eventService.getEventsWithRegistrationCounts();
            return ResponseEntity.ok(eventsWithCounts);
        } else {
            return ResponseEntity.status(403).build();
        }
    }
	
}
