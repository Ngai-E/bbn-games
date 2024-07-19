package com.cypherfund.bbn.dao.repository;

import com.cypherfund.bbn.dao.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByTournamentIdAndEventDateAfter(Long tournamentId, Instant date);
}