package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.AttackService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Player;

import java.util.List;


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
    
    @PutMapping("/games/{gameCode}/attacks")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO setAttacks(@RequestHeader("Authorization") String token, @PathVariable("gameCode") String gameCode, @RequestBody List<String> attacks) {
        
        Player updatedPlayer = attackService.setAttacks(gameCode, attacks, token);

        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(updatedPlayer);
    }
}
