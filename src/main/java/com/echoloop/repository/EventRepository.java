package com.echoloop.repository;

import com.echoloop.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByGenre(String genre);
    List<Event> findByGenreIn(List<String> genres);
    List<Event> findByCreatedById(Long userId); // ← Add this line
    List<Event> findByCreatedByRole(String role);
    Optional<Event> findById(Long id);


}
