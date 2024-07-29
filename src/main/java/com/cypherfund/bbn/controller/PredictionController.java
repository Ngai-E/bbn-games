package com.cypherfund.bbn.controller;


import com.cypherfund.bbn.dao.specifications.filters.BetFilterCriteria;
import com.cypherfund.bbn.dto.BetDto;
import com.cypherfund.bbn.models.PredictionRequest;
import com.cypherfund.bbn.services.contract.IBettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bets")
@RequiredArgsConstructor
public class PredictionController {
    private final IBettingService bettingService;

    @PostMapping
    public void placebet(@RequestBody PredictionRequest predictionRequest) {
        bettingService.placeBet(predictionRequest);
    }

    @GetMapping("/tickets/{userId}") //should also filter by date
    public List<BetDto> getUserTickets(@PathVariable String userId,
                                        @RequestParam(required = false) Integer jackpotId,
                                        @RequestParam(required = false) String startDate,
                                        @RequestParam(required = false) String endDate,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        BetFilterCriteria betFilterCriteria = BetFilterCriteria.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .jackpotId(jackpotId)
                .build();
        return bettingService.getUserBets(betFilterCriteria, page, size);
    }
}
