package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.LeaderboardService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardGetDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
@Tag(name = "Leaderboard", description = "Leaderboard endpoints")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/leaderboard")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get the leaderboard", description = "Returns the leaderboard sorted by wins")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Leaderboard returned"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<LeaderboardGetDTO> getLeaderboard() {
        return leaderboardService.getLeaderboard();
    }
}