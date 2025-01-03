package com.springprjt.springboot.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springprjt.springboot.model.Event;
import com.springprjt.springboot.repository.EventRepository;
import com.springprjt.springboot.repository.RegistrationRepository;

@Service
public class EventService {
		
	@Autowired
    private EventRepository eventRepository;
	
	@Autowired
	private RegistrationRepository registrationRepository;

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }    
    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);  
    }
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
    
    public List<Event> getEventsByNameOrLocation(String title, String location) {
        if (title != null && location != null) {
            return eventRepository.findByTitleAndLocation(title, location);
        } else if (title != null) {
            return eventRepository.findByTitle(title);
        } else if (location != null) {
            return eventRepository.findByLocation(location);
        } else {
            return new ArrayList<>(); // Return empty list if neither is provided
        }
    }

    public Map<Event, Long> getEventsWithRegistrationCounts() {
        List<Event> events = eventRepository.findAll();
        Map<Event, Long> eventData = new HashMap<>();

        for (Event event : events) {
            long registrationCount = registrationRepository.findByEvent(event).size();
            eventData.put(event, registrationCount);
        }

        return eventData;
    }
}
