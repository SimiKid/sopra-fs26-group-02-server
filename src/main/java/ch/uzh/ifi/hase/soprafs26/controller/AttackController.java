package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.AttackService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * GameSession Controller
 * This class is responsible for handling all REST request that are related to
 * the game session.
 * The controller will receive the request and delegate the execution to the
 * GameSessionService and finally return the result.
 */


@RestController
public class AttackController {
    private final AttackService attackService;
    private final AuthenticationService authenticationService;

    AttackController(AttackService attackService, AuthenticationService authenticationService) {
        this.attackService = attackService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/attacks")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<AttackGetDTO> getAttacks() {
        return attackService.getAllAttacks();
    }
    
    @PutMapping("/game/{gameCode}/players/{userId}/attacks")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO setAttacks(@RequestHeader("Authorization") String token, @PathVariable("gameCode") String gameCode, @PathVariable("userId") Long userId, @RequestBody List<String> attacks) {
        User user = authenticationService.authenticateByToken(token);
        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not allowed to set attacks for this player");
        }   
        Player updatedPlayer = attackService.setAttacks(gameCode, userId, attacks);

        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(updatedPlayer);
    }
}
	// Helper for protected endpoints (Task #76 - Session Management):
	// To secure a endpoint, follow these two steps:
	// 1. Add '@RequestHeader("Authorization") String token' as a method parameter.
	// 2. Call 'authenticationService.authenticateByToken(token);' as the first line of the method.
	// If the token is invalid or missing, an UNAUTHORIZED (401) exception will be thrown automatically.
