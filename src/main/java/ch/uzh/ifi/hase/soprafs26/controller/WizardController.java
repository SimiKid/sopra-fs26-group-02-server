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

    private final AuthenticationService authenticationService;
    private final GameSessionService gameSessionService;

    public WizardController(AuthenticationService authenticationService, GameSessionService gameSessionService) {
        this.authenticationService = authenticationService;
        this.gameSessionService = gameSessionService;
    }
    
    // Endpoint to get the list of available wizard classes
    @GetMapping("/wizard")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<WizardClass> getWizards(@RequestHeader("Authorization") String token) {
        authenticationService.authenticateByToken(token);
        return Arrays.asList(WizardClass.values());
    }
    
    @PutMapping("/game/{gameCode}/players/{userId}/wizard")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO putWizard (@RequestHeader("Authorization") String token, @PathVariable("gameCode") String gameCode, @PathVariable("userId") Long userId, @RequestBody PlayerPutDTO playerPutDTO) {
        authenticationService.authenticateByToken(token);
        Player player = gameSessionService.saveWizardClass(gameCode, userId, playerPutDTO.getWizardClass());
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player);
    }
    
}
