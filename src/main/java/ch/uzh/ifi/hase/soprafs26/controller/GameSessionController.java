package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



/**
 * GameSession Controller
 * This class is responsible for handling all REST request that are related to
 * the game session.
 * The controller will receive the request and delegate the execution to the
 * GameSessionService and finally return the result.
 */


@RestController
@Tag(name = "Game Session", description = "Game session management endpoints")
public class GameSessionController {

	private final AuthenticationService authenticationService;
    private final GameSessionService gameSessionService;

	GameSessionController(GameSessionService gameSessionService, AuthenticationService authenticationService) {
		this.gameSessionService = gameSessionService;
        this.authenticationService = authenticationService;
	}

    @PostMapping("/game")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Operation(summary = "Create a new game session", description = "Creates a new game session with the authenticated user as player 1")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Game session successfully created"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    public GameSessionGetDTO createGameSession(@RequestHeader("Authorization") String token) {
		User creator=authenticationService.authenticateByToken(token);
        GameSession newGameSession = new GameSession();
        newGameSession.setPlayer1Id(creator.getId());
        GameSession createdGameSession = gameSessionService.createGameSession(newGameSession);
        return DTOMapper.INSTANCE.convertEntityToGameSessionGetDTO(createdGameSession);
    }

    @GetMapping("/game/{gameCode}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get a game session by code", description = "Retrieves the game session details for the given game code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game session found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "Game session not found")
    })
    public GameSessionGetDTO getGameSessionByCode(
            @Parameter(description = "The unique game code") @PathVariable("gameCode") String gameCode) {
        GameSession gameSession = gameSessionService.getByGameCode(gameCode);
        return DTOMapper.INSTANCE.convertEntityToGameSessionGetDTO(gameSession);
    }

    @PutMapping("/game/{gameCode}/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Join an existing game session", description = "Adds the authenticated user as player 2 to the game session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined the game session"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "Game session not found"),
        @ApiResponse(responseCode = "409", description = "Game session is already full or expired")
    })
    public GameSessionGetDTO joinGameSession(
            @Parameter(description = "The unique game code") @PathVariable("gameCode") String gameCode,
            @RequestHeader("Authorization") String token) {
        User joiner = authenticationService.authenticateByToken(token);
        GameSession updatedGameSession = gameSessionService.joinGameSession(gameCode, joiner.getId());
        return DTOMapper.INSTANCE.convertEntityToGameSessionGetDTO(updatedGameSession);
    }

    @DeleteMapping("/game/{gameCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a game session", description = "Deletes the game session with the given game code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Game session successfully deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "Game session not found")
    })
    public void deleteGameSession(
            @Parameter(description = "The unique game code") @PathVariable("gameCode") String gameCode) {
        gameSessionService.deleteByGameCode(gameCode);
    }
}
	