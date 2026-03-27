package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;


/**
 * GameSession Controller
 * This class is responsible for handling all REST request that are related to
 * the game session.
 * The controller will receive the request and delegate the execution to the
 * GameSessionService and finally return the result.
 */


@RestController
public class GameSessionController {

	private final GameSessionService gameSessionService;

	GameSessionController(GameSessionService gameSessionService) {
		this.gameSessionService = gameSessionService;
	}

    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public GameSessionGetDTO createGameSession(@RequestBody GameSessionPostDTO gameSessionPostDTO) {
		// convert API game session to internal representation
        GameSession gameSessionInput = DTOMapper.INSTANCE.convertGameSessionPostDTOtoEntity(gameSessionPostDTO);
		// create game session
        GameSession createdGameSession = gameSessionService.createGameSession(gameSessionInput);
		// convert internal representation of game session back to API
        return DTOMapper.INSTANCE.convertEntityToGameSessionGetDTO(createdGameSession);
    }

    @GetMapping("/games/{gameCode}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GameSessionGetDTO getGameSessionByCode(@PathVariable String gameCode) {
        GameSession gameSession = gameSessionService.getByGameCode(gameCode);
        return DTOMapper.INSTANCE.convertEntityToGameSessionGetDTO(gameSession);
    }
}
