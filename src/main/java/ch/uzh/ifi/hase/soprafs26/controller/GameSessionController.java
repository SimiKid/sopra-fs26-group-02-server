package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;



/**
 * GameSession Controller
 * This class is responsible for handling all REST request that are related to
 * the game session.
 * The controller will receive the request and delegate the execution to the
 * GameSessionService and finally return the result.
 */


@RestController
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
    public GameSessionGetDTO getGameSessionByCode(@PathVariable("gameCode") String gameCode,@RequestHeader("Authorization") String token) {
        authenticationService.authenticateByToken(token);
        GameSession gameSession = gameSessionService.getByGameCode(gameCode);
        return DTOMapper.INSTANCE.convertEntityToGameSessionGetDTO(gameSession);
    }


}
	// Helper for protected endpoints (Task #76 - Session Management):
	// To secure a endpoint, follow these two steps:
	// 1. Add '@RequestHeader("Authorization") String token' as a method parameter.
	// 2. Call 'authenticationService.authenticateByToken(token);' as the first line of the method.
	// If the token is invalid or missing, an UNAUTHORIZED (401) exception will be thrown automatically.
