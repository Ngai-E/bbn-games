package com.cypherfund.bbn.services.impl;

import com.cypherfund.bbn.dao.entity.Bet;
import com.cypherfund.bbn.dao.entity.BetItem;
import com.cypherfund.bbn.dao.entity.Ticket;
import com.cypherfund.bbn.dao.repository.AuditLogRepository;
import com.cypherfund.bbn.dao.repository.BetItemRepository;
import com.cypherfund.bbn.dao.repository.BetRepository;
import com.cypherfund.bbn.dao.repository.TicketRepository;
import com.cypherfund.bbn.models.ApiResponse;
import com.cypherfund.bbn.models.DebitRequest;
import com.cypherfund.bbn.models.PredictionRequest;
import com.cypherfund.bbn.proxies.UserFeignClient;
import com.cypherfund.bbn.utils.Enumerations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IBettingServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private BetItemRepository betItemRepository;
    @Mock
    private UserFeignClient userFeignClient;
    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private IBettingServiceImpl bettingService;

    private PredictionRequest predictionRequest;

    @BeforeEach
    void setUp() {
        PredictionRequest.Bet.Event event1 = new PredictionRequest.Bet.Event();
        event1.setEventId(1L);
        event1.setPrediction(1L);
        event1.setOdds(new BigDecimal("2.0"));

        PredictionRequest.Bet bet1 = new PredictionRequest.Bet();
        bet1.setBetType(Enumerations.BetType.SINGLE);
        bet1.setAmount(new BigDecimal("100"));
        bet1.setEvents(List.of(event1));

        predictionRequest = new PredictionRequest();
        predictionRequest.setUserId("101");
        predictionRequest.setTicketType(Enumerations.TicketType.ODDS);
        predictionRequest.setBets(List.of(bet1));
    }

    @Test
    void placeBet() {
        Ticket savedTicket = new Ticket();
        savedTicket.setId(1L);
        savedTicket.setTotalAmount(new BigDecimal("100.0"));
        savedTicket.setTotalOdds(new BigDecimal("2.0"));
        savedTicket.setType(Enumerations.TicketType.ODDS);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        Bet savedBet = new Bet();
        savedBet.setId(1L);
        when(betRepository.save(any(Bet.class))).thenReturn(savedBet);

        BetItem savedBetItem = new BetItem();
        savedBetItem.setId(1L);
        when(betItemRepository.save(any(BetItem.class))).thenReturn(savedBetItem);

        ApiResponse<String> response =  ApiResponse.success("Bet placed successfully");
        when(userFeignClient.play(any(DebitRequest.class))).thenReturn(response);

        bettingService.placeBet(predictionRequest);

        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(betRepository, times(1)).save(any(Bet.class));
        verify(betItemRepository, times(1)).save(any(BetItem.class));
    }

    @Test
    void calculatePotentialWinnings() {
        BigDecimal potentialWinnings = bettingService.calculatePotentialWinnings(predictionRequest.getBets().get(0));
        assertEquals(new BigDecimal("200.0"), potentialWinnings);
    }

}
