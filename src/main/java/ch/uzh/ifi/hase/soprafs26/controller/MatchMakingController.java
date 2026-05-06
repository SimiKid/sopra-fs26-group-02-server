package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import ch.uzh.ifi.hase.soprafs26.service.MatchMakingService;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
public class MatchMakingController {
    
    private final MatchMakingService matchMakingService;
    private final AuthenticationService authenticationService;
    public MatchMakingController(MatchMakingService matchMakingService, AuthenticationService authenticationService) {
        this.matchMakingService = matchMakingService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/matchmaking/join")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Join a random game session", description = "Joins a random game session for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game session joined successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    public void joinRandomGameSession(@RequestHeader("Authorization") String token) {
        Long userId = authenticationService.authenticateByToken(token).getId();
        matchMakingService.joinRandomGameSession(userId);
    }

    @DeleteMapping("/matchmaking/leave")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Leave a waiting game session", description = "Leaves a waiting game session for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Game session left successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    public void leaveGameSession(@RequestHeader("Authorization") String token) {
        Long userId = authenticationService.authenticateByToken(token).getId();
        matchMakingService.leaveGameSession(userId);
    }
}
