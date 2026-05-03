package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.BattleResult;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleResultGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.BattleService;

/**
 * REST read-only endpoints for battle state and final result.
 * Real-time updates during a battle go over WebSocket
 * (see BattleWebSocketController + /topic/game/{code}).
 */
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

    @GetMapping("/games/{gameCode}/result")
    @ResponseStatus(HttpStatus.OK)
    public BattleResultGetDTO getBattleResult(@PathVariable("gameCode") String gameCode){
        BattleResult result = battleService.getBattleResult(gameCode);
        return DTOMapper.INSTANCE.convertBattleResultToGetDTO(result);
    }        
}