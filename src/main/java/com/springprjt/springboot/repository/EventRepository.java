package com.springprjt.springboot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.springprjt.springboot.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCreatedBy(String createdBy);

    List<Event> findByTitle(String title);

    List<Event> findByLocation(String location);

    List<Event> findByTitleAndLocation(String title, String location);
}
