package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.AttackService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
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

    public AttackController(AttackService attackService) {
        this.attackService = attackService;
    }

    @GetMapping("/attacks")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<AttackGetDTO> getAttacks() {
        return attackService.getAllAttacks();
    }

}
	// Helper for protected endpoints (Task #76 - Session Management):
	// To secure a endpoint, follow these two steps:
	// 1. Add '@RequestHeader("Authorization") String token' as a method parameter.
	// 2. Call 'authenticationService.authenticateByToken(token);' as the first line of the method.
	// If the token is invalid or missing, an UNAUTHORIZED (401) exception will be thrown automatically.
