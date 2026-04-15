package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.Interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.AttackService;
import ch.uzh.ifi.hase.soprafs26.entity.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;



import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttackController.class)
public class AttackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttackService attackService;

    @MockitoBean
    private AuthInterceptor authInterceptor;
    
    @BeforeEach
    void setup() {
        Mockito.when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    private static AttackGetDTO toDto(Attack attack) {
        AttackGetDTO dto = new AttackGetDTO();
        dto.setId(attack.getId());
        dto.setName(attack.getDisplayName());
        dto.setBaseDamage(attack.getBaseDamage());
        dto.setElement(attack.getElement());
        dto.setDescription(attack.getDescription());
        return dto;
    }

    @Test
    void getAttacks_returnsOkAndBodyFromService() throws Exception {
        List<AttackGetDTO> attacks = Stream.of(Attack.values())
            .map(AttackControllerTest::toDto)
            .collect(Collectors.toList());
        given(attackService.getAllAttacks()).willReturn(attacks);

        Attack sample = Attack.values()[0];

        mockMvc.perform(get("/attacks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(Attack.values().length)))
            .andExpect(jsonPath("$[0].id", is(sample.getId())))
            .andExpect(jsonPath("$[0].name", is(sample.getDisplayName())))
            .andExpect(jsonPath("$[0].baseDamage", is(sample.getBaseDamage())))
            .andExpect(jsonPath("$[0].element", is(sample.getElement().name())))
            .andExpect(jsonPath("$[0].description", is(sample.getDescription())));

        verify(attackService).getAllAttacks();
    }

    @Test
    void setAttacks_success_returnsOk() throws Exception {
        // given
        String gameCode = "G123";
        Long userId = 1L;
        String token = "test-token";
        List<String> attackList = List.of("FIREBALL", "ICE_SHIELD", "HEAL");

        Player player = new Player();
        player.setUserId(userId);
        player.setAttack1("FIREBALL");
        player.setAttack2("ICE_SHIELD");
        player.setAttack3("HEAL");
        player.setReady(true);

        given(attackService.setAttacks(eq(gameCode), any(), eq(token)))
                .willReturn(player);

        // when then
        mockMvc.perform(put("/games/" + gameCode + "/attacks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(attackList))) // Wandelt die Liste in JSON um
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(userId.intValue())))
                .andExpect(jsonPath("$.attacks[0]", is("FIREBALL")))
                .andExpect(jsonPath("$.attacks[1]", is("ICE_SHIELD")))
                .andExpect(jsonPath("$.attacks[2]", is("HEAL")))
                .andExpect(jsonPath("$.ready", is(true)));

        verify(attackService).setAttacks(eq(gameCode), any(), eq(token));
    }

    @Test
    void setAttacks_invalidAttacks_returnsBadRequest() throws Exception {
        String token = "test-token";
        List<String> shortList = List.of("FIREBALL");

        given(attackService.setAttacks(any(), any(), any()))
            .willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exactly 3 attacks must be selected."));

        mockMvc.perform(put("/games/G123/attacks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(shortList)))
                .andExpect(status().isBadRequest());
    }
}
