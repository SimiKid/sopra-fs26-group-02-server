package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleResultGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
import ch.uzh.ifi.hase.soprafs26.service.BattleService;

@RestController
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @GetMapping("/games/{gameCode}/battles")
    @ResponseStatus(HttpStatus.OK)
    public BattleStateDTO getBattleState(@PathVariable("gameCode") String gameCode) {
        return battleService.getBattleState(gameCode);
    }

    @GetMapping("/games/{gameCode}/battles/result")
    @ResponseStatus(HttpStatus.OK)
    public BattleResultGetDTO getBattleResult(@PathVariable("gameCode") String gameCode){
        return battleService.getBattleResult(gameCode);
    }        
}