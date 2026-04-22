package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.service.RematchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Rematch", description = "Rematch endpoints")
public class RematchController {

    private final RematchService rematchService;

    RematchController(RematchService rematchService) {
        this.rematchService = rematchService;
    }

    @PostMapping("/games/{gameCode}/rematch")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request a rematch", description = "Registers this player's rematch request. When both players have requested, a new game is created.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rematch request registered"),
        @ApiResponse(responseCode = "403", description = "Not a participant of this game"),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "409", description = "Game is not finished yet")
    })
    public void requestRematch(
            @Parameter(description = "The game code of the finished game") @PathVariable("gameCode") String gameCode,
            @RequestHeader("Authorization") String token) {
        rematchService.requestRematch(gameCode, token);
    }
}