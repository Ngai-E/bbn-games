package com.cypherfund.bbn.services.impl;

import com.cypherfund.bbn.dao.entity.*;
import com.cypherfund.bbn.dao.repository.*;
import com.cypherfund.bbn.dto.CategoryDto;
import com.cypherfund.bbn.dto.EventDto;
import com.cypherfund.bbn.dto.OutcomeDto;
import com.cypherfund.bbn.dto.TournamentDto;
import com.cypherfund.bbn.exception.AppException;
import com.cypherfund.bbn.models.CreateOutcomeRequest;
import com.cypherfund.bbn.utils.Enumerations;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final GameRepository gameRepository;
    private final CategoryRepository categoryRepository;
    private final TournamentRepository tournamentRepository;
    private final EventRepository eventRepository;
    private final OutcomeRepository outcomeRepository;
    private final EventTypeTemplateRepository eventTypeTemplateRepository;
    private final HousemateRepository housemateRepository;
    private final ModelMapper modelMapper;


    // Game management
    public Game createGame(Game game) {
        return gameRepository.save(game);
    }

    public Game updateGame(Long id, Game gameDetails) {
        Game game = gameRepository.findById(id).orElseThrow(() -> new AppException("Game not found"));
        game.setName(gameDetails.getName());
        return gameRepository.save(game);
    }

    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }

    // Category management
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setGame(gameRepository.findById(categoryDto.getGameId()).orElseThrow(() -> new AppException("Game not found")));
        return modelMapper.map(categoryRepository.save(category), CategoryDto.class);
    }

    public CategoryDto updateCategory(Long id, CategoryDto categoryDetails) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new AppException("Category not found"));
        category.setName(categoryDetails.getName());
        category.setGame(gameRepository.findById(categoryDetails.getGameId()).orElseThrow(() -> new AppException("Game not found")));
        return modelMapper.map(categoryRepository.save(category), CategoryDto.class);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // Tournament management
    public TournamentDto createTournament(TournamentDto tournamentDto) {
        Tournament tournament = new Tournament();
        buildTournamentEntity(tournamentDto, tournament);
        return modelMapper.map(tournamentRepository.save(tournament), TournamentDto.class);
    }

    public TournamentDto updateTournament(Long id, TournamentDto tournamentDetails) {
        Tournament tournament = tournamentRepository.findById(id).orElseThrow(() -> new AppException("Tournament not found"));
        buildTournamentEntity(tournamentDetails, tournament);
        return modelMapper.map(tournamentRepository.save(tournament), TournamentDto.class);
    }

    public void deleteTournament(Long id) {
        tournamentRepository.deleteById(id);
    }

    // Event management
    public EventDto createEvent(EventDto eventDto) {
        Event event = new Event();
        buildEventEntity(eventDto, event);
        return modelMapper.map(eventRepository.save(event), EventDto.class);
    }

    public EventDto updateEvent(Integer id, EventDto eventDetails) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new AppException("Event not found"));
        buildEventEntity(eventDetails, event);
        event.setTournament(tournamentRepository.findById(eventDetails.getTournamentId()).orElseThrow(() -> new AppException("Tournament not found")));
        return modelMapper.map(eventRepository.save(event), EventDto.class);
    }

    public void deleteEvent(Integer id) {
        eventRepository.deleteById(id);
    }

    // Outcome management
    public OutcomeDto createOutcome(CreateOutcomeRequest createOutcomeRequest) {
        Outcome outcome = new Outcome();
        buildOutcomeEntity(createOutcomeRequest, outcome);
        return modelMapper.map(outcomeRepository.save(outcome), OutcomeDto.class);
    }

    public OutcomeDto updateOutcome(Long id, OutcomeDto outcomeDetails) {
        Outcome outcome = outcomeRepository.findById(id).orElseThrow(() -> new AppException("Outcome not found"));
        outcome.setOdds(outcomeDetails.getOdds());
        return modelMapper.map(outcomeRepository.save(outcome), OutcomeDto.class);
    }

    public void deleteOutcome(Long id) {
        outcomeRepository.deleteById(id);
    }

    // Get events and outcomes by tournament
    public List<EventDto> getEventsByTournament(Long tournamentId) {
        return eventRepository.findByTournamentId(tournamentId).stream()
                .map((element) -> modelMapper.map(element, EventDto.class))
                .toList();
    }

    public List<OutcomeDto> getOutcomesByEvent(Long eventId) {
        return outcomeRepository.findByEventId(eventId).stream()
                .map((element) -> modelMapper.map(element, OutcomeDto.class))
                .toList();
    }

    public List<Game> getGames() {
        return gameRepository.findAll();
    }

    public List<CategoryDto> gameCategories(long gameId) {
        return categoryRepository.findByGameId(gameId).stream()
                .map((element) -> modelMapper.map(element, CategoryDto.class))
                .toList();
    }

    private void buildOutcomeEntity(CreateOutcomeRequest createOutcomeRequest, Outcome outcome) {
        Event event = eventRepository.findById(createOutcomeRequest.getEventId()).orElseThrow(() -> new AppException("Event not found"));

        Map<Integer, Housemate> housemates = housemateRepository.findHousematesByIdIn(createOutcomeRequest.getHouseIds()).stream()
                .collect(Collectors.toMap(Housemate::getId, housemate -> housemate));

        if (housemates.size() != createOutcomeRequest.getHouseIds().size()) {
            throw new AppException("Housemate not found");
        }

        if (event.getEventTypeTemplate().getHousemateNum() != createOutcomeRequest.getHouseIds().size()) {
            throw new AppException("Housemate number does not match");
        }

        String description = event.getEventTypeTemplate().getTemplate();
        for (int i = 1; i <= event.getEventTypeTemplate().getHousemateNum(); i++) {
            description = description.replace("{" + i + "}", housemates.get(createOutcomeRequest.getHouseIds().get(i - 1)).getName());
        }
        outcome.setDescription(description);
        outcome.setOdds(BigDecimal.valueOf(createOutcomeRequest.getOdds()));
        outcome.setEvent(event);
    }

    private void buildTournamentEntity(TournamentDto tournamentDto, Tournament tournament) {
        tournament.setName(tournamentDto.getName());
        tournament.setStartDate(tournamentDto.getStartDate());
        tournament.setEndDate(tournamentDto.getEndDate());
        tournament.setCategory(categoryRepository.findById(tournamentDto.getCategoryId()).orElseThrow(() -> new AppException("Category not found")));
    }

    private void buildEventEntity(EventDto eventDto, Event event) {
        EventTypeTemplate eventTypeTemplate = eventTypeTemplateRepository
                .findById(eventDto.getEventTypeTemplateId())
                .orElseThrow(() -> new AppException("Event type template not found"));
        event.setName(eventDto.getName());
        event.setEventDate(eventDto.getEventDate());
        event.setStatus(Enumerations.Event_Status.PENDING);
        event.setType(eventDto.getType());
        event.setEventTypeTemplate(eventTypeTemplate);
        event.setTournament(tournamentRepository.findById(eventDto.getTournamentId()).orElseThrow(() -> new AppException("Tournament not found")));
    }
}

