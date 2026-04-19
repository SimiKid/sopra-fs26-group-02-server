package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



/**
 * Location Controller
 * This class is responsible for handling all REST request that are related to
 * the location information for a game session.
 * The controller will receive the request and delegate the execution to the
 * GameSessionService and finally return the result.
 */


@RestController
@Tag(name = "Location", description = "Location information endpoints")
public class LocationController {

    private final GameSessionService gameSessionService;

	LocationController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
	}

    @GetMapping("/games/{gameCode}/location")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get location information for a specific game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved location information"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })

    public String getLocationForGame(@PathVariable String gameCode) {
        return gameSessionService.getLocationByCode(gameCode);
    }
}