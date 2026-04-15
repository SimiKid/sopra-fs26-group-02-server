package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;



@RestController
public class WizardController {

    private final GameSessionService gameSessionService;

    public WizardController(AuthenticationService authenticationService, GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }
    
    // Endpoint to get the list of available wizard classes
    @GetMapping("/wizards")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<WizardClass> getWizards() {
        return Arrays.asList(WizardClass.values());
    }
    
    @PutMapping("/games/{gameCode}/wizards")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO putWizard (@RequestHeader("Authorization") String token, @PathVariable ("gameCode") String gameCode, @RequestBody PlayerPutDTO playerPutDTO) {
        Player player = gameSessionService.saveWizardClass(gameCode, token, playerPutDTO.getWizardClass());
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
    }
    
}
