package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.AttackService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Player;

import java.util.List;
import java.util.stream.Collectors;


/**
 * REST endpoints for the attack-selection phase: listing all available
 * attacks and saving a player's 3 chosen attacks for a given game.
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
        return attackService.getAllAttacks().stream()
            .map(DTOMapper.INSTANCE::convertAttackToAttackGetDTO)
            .collect(Collectors.toList());
    }
    
    @PutMapping("/games/{gameCode}/attacks")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO setAttacks(@RequestHeader("Authorization") String token, @PathVariable("gameCode") String gameCode, @RequestBody List<String> attacks) {

        Player updatedPlayer = attackService.setAttacks(gameCode, attacks, token);

        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(updatedPlayer);
    }

    @GetMapping("/games/{gameCode}/attacks")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO getSelectedAttacks(@RequestHeader("Authorization") String token, @PathVariable("gameCode") String gameCode) {
        Player player = attackService.getAttacks(gameCode, token);
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
    }
}
