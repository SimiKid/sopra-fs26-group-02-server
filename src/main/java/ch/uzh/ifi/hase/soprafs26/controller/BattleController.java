package ch.uzh.ifi.hase.soprafs26.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import ch.uzh.ifi.hase.soprafs26.service.BattleService;

@RestController
public class BattleController {
    private final Logger log = LoggerFactory.getLogger(BattleController.class);
    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping("/games/{gameCode}/battles/turns")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public int[] turnBattle(@RequestHeader("Authorization") String token, @PathVariable("gameCode") String gameCode, @RequestBody String attack) {
        int[] result = battleService.turnBattle(gameCode, attack);
        return result;
    }
}
