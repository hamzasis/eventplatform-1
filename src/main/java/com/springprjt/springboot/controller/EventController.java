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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<?> getEvents(@RequestHeader("Authorization") String token, 
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "location", required = false) String location) {
        try {
            // Print the token (for debugging)
            String extractedToken = token.substring(7);  // Remove "Bearer "
            String role = jwtUtil.extractRole(extractedToken);
            
            if ("ADMIN".equalsIgnoreCase(role) || "ORGANIZER".equalsIgnoreCase(role)) {
                List<Event> events = eventService.getEventsByNameOrLocation(title, location);

                if (events.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events found matching the search criteria.");
                }

                return ResponseEntity.ok(events);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. You do not have permission.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or malformed token.");
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventByNameOrLocation(
            @RequestHeader("Authorization") String token,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "location", required = false) String location) {
        try {
            // Debug: Print raw token
            System.out.println("Authorization Header: " + token);

            // Remove "Bearer " prefix
            String extractedToken = token.substring(7);
            System.out.println("Extracted Token: " + extractedToken);

            // Print token content (optional, for debugging)
            jwtUtil.printTokenContent(extractedToken);

            // Extract role
            String role = jwtUtil.extractRole(extractedToken);
            System.out.println("Role extracted from token: " + role);

            // Check role (any authenticated user can view an event)
            if ("ADMIN".equalsIgnoreCase(role) ||
                "ORGANIZER".equalsIgnoreCase(role)) {

                // Search by name or location (at least one parameter must be provided)
                if (title != null || location != null) {
                    List<Event> events = eventService.getEventsByNameOrLocation(title, location);
                    
                    if (!events.isEmpty()) {
                        System.out.println("Found " + events.size() + " events matching the search criteria.");
                        return ResponseEntity.ok(events);
                    } else {
                        System.out.println("No events found matching the search criteria.");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("No events found.");
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Please provide either a name or location for search.");
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
