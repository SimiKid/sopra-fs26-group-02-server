package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import ch.uzh.ifi.hase.soprafs26.service.TimerService;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDateTime;

@RestController
public class TimerController {

    private final TimerService timerService;

    public TimerController(TimerService timerService) {
        this.timerService = timerService;
    }

    @GetMapping("/timer/{gameCode}")
    @Operation(summary = "Get the end of the turn", description = "Returns the end of the turn for a given game code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "End of turn returned"),
        @ApiResponse(responseCode = "404", description = "Game session not found")
    })
    public LocalDateTime getEndofTurn(@PathVariable("gameCode") String gameCode) {
        return timerService.getEndofTurn(gameCode);
    }
    
}
