package ch.uzh.ifi.hase.soprafs26.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;


@WebMvcTest(WizardController.class)
public class WizardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthenticationService authenticationService;

	@MockitoBean	
	private GameSessionService gameSessionService;

	@Test
    public void getWizards_validToken_returnsAllClasses() throws Exception {

        //when then
        mockMvc.perform(get("/wizards")
                .header("Authorization", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0]").value("ATTACKWIZARD"));
    }

    @Test
    public void getWizards_invalidToken_returnsUnauthorized() throws Exception {
        //given
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED))
            .when(authenticationService).authenticateByToken("invalid Token");
        
        //when then
        mockMvc.perform(get("/wizards")
                .header("Authorization", "invalid Token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void putWizard_validInput_returnsPlayer() throws Exception {
        //given
        Player player = new Player();
        player.setWizardClass(WizardClass.ATTACKWIZARD);
        player.setHp(100);

        given(gameSessionService.saveWizardClass("123", "valid-token", "ATTACKWIZARD")).willReturn(player);

        //when then
        mockMvc.perform(put("/game/123/wizard")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wizardClass\": \"ATTACKWIZARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wizardClass").value("ATTACKWIZARD"))
                .andExpect(jsonPath("$.hp").value(100));             
    }

    @Test
    public void putWizard_invalidToken_returnsUnauthorized() throws Exception {
        //given
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED))
            .when(authenticationService).authenticateByToken
            ("invalid Token");
        //when then
        mockMvc.perform(put("/game/123/wizard")
                .header("Authorization", "invalid Token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wizardClass\": \"ATTACKWIZARD\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void putWizard_invalidWizardClass_returnsBadRequest() throws Exception {
        //given
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST))
            .when(gameSessionService).saveWizardClass("123", "valid-token", "INVALIDCLASS");
        
        //when then
        mockMvc.perform(put("/game/123/wizard")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wizardClass\": \"INVALIDCLASS\"}"))
                .andExpect(status().isBadRequest());
    }
}
