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

    AttackController(AttackService attackService) {
        this.attackService = attackService;
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
        
        Player updatedPlayer = attackService.setAttacks(gameCode, userId, attacks, token);

        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(updatedPlayer);
    }
}
	// Helper for protected endpoints (Task #76 - Session Management):
	// To secure a endpoint, follow these two steps:
	// 1. Add '@RequestHeader("Authorization") String token' as a method parameter.
	// 2. Call 'authenticationService.authenticateByToken(token);' as the first line of the method.
	// If the token is invalid or missing, an UNAUTHORIZED (401) exception will be thrown automatically.
