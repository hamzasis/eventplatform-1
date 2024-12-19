package com.springprjt.springboot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springprjt.springboot.model.Event;
import com.springprjt.springboot.model.Registration;
import com.springprjt.springboot.model.User;
import com.springprjt.springboot.repository.EventRepository;
import com.springprjt.springboot.repository.RegistrationRepository;
import com.springprjt.springboot.repository.UserRepository;

@Service
public class RegistrationService {

	@Autowired
	private RegistrationRepository registrationRepository;
	
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;
    
    public String registerUserForEvent(Long eventId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);

        if (user == null || event == null) {
            return "User or Event not found.";
        }

        if (registrationRepository.existsByUserAndEvent(user, event)) {
            return "User is already registered for this event.";
        }

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus("PENDING");

        registrationRepository.save(registration);
        return "Registration successful. Pending approval.";
    }
    public String updateRegistrationStatus(Long registrationId, String status) {
        Registration registration = registrationRepository.findById(registrationId).orElse(null);

        if (registration == null) {
            return "Registration not found.";
        }

        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            return "Invalid status. Must be 'APPROVED' or 'REJECTED'.";
        }

        registration.setStatus(status);
        registrationRepository.save(registration);
        return "Registration status updated to " + status;
    }

    public List<Registration> getRegistrationsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return null;
        }
        return registrationRepository.findByEvent(event);
    }

    public long countRegistrationsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return 0;
        }
        return registrationRepository.findByEvent(event).size();
    }
}
