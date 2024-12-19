package com.springprjt.springboot.repository;

import com.springprjt.springboot.model.Event;
import com.springprjt.springboot.model.Registration;
import com.springprjt.springboot.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByUserAndEvent(User user, Event event);
    List<Registration> findByEvent(Event event);
}
