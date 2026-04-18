package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



/**
 * Weather Controller
 * This class is responsible for handling all REST request that are related to
 * the weather information for a game session.
 * The controller will receive the request and delegate the execution to the
 * GameSessionService and finally return the result.
 */


@RestController
@Tag(name = "Weather", description = "Weather information endpoints")
public class WeatherController {

    private final GameSessionService gameSessionService;

	WeatherController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
	}

    @GetMapping("/games/{gameCode}/weather")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get weather information for a specific game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved weather information"),
            @ApiResponse(responseCode = "404", description = "Game not found, fallback to default weather")
    })

    public WeatherGetDTO getWeatherForGame(@PathVariable String gameCode) {
        return gameSessionService.getWeatherByCode(gameCode);
    }
}