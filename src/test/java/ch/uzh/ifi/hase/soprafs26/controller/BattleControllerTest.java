package ch.uzh.ifi.hase.soprafs26.controller;

import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleResultGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.BattleService;

@WebMvcTest(BattleController.class)
public class BattleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @MockitoBean
    private BattleService battleService;

    @BeforeEach
    void setup() {
        Mockito.when(authInterceptor.preHandle(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(true);
    }

    @Test
    void getBattleState_success_returnsOk() throws Exception {
        BattleStateDTO dto = new BattleStateDTO();
        dto.setActivePlayerId(1L);
        dto.setPlayer1Hp(100);
        dto.setPlayer2Hp(80);

        given(battleService.getBattleState("ABC123")).willReturn(dto);

        mockMvc.perform(get("/games/ABC123/battles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activePlayerId", is(1)))
            .andExpect(jsonPath("$.player1Hp", is(100)))
            .andExpect(jsonPath("$.player2Hp", is(80)));
    }

    @Test
    void getBattleState_notFound_returns404() throws Exception {
        given(battleService.getBattleState("INVALID"))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/games/INVALID/battles"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getBattleResult_success_returnsOk() throws Exception {
        WeatherGetDTO weather = new WeatherGetDTO();
        weather.setRainCategory(RainCategory.CLEAR);
        weather.setTemperatureCategory(TemperatureCategory.HOT);

        BattleResultGetDTO dto = new BattleResultGetDTO();
        dto.setWinnerUserId(1L);
        dto.setLoserUserId(2L);
        dto.setTotalDamageDealt(150);
        dto.setTurnsPlayed(5);
        dto.setWeather(weather);

        given(battleService.getBattleResult("ABC123")).willReturn(dto);

        mockMvc.perform(get("/games/ABC123/battles/result"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.winnerUserId", is(1)))
            .andExpect(jsonPath("$.loserUserId", is(2)))
            .andExpect(jsonPath("$.totalDamageDealt", is(150)))
            .andExpect(jsonPath("$.turnsPlayed", is(5)))
            .andExpect(jsonPath("$.weather.rainCategory", is("CLEAR")))
            .andExpect(jsonPath("$.weather.temperatureCategory", is("HOT")));
    }

    @Test
    void getBattleResult_notFound_returns404() throws Exception {
        given(battleService.getBattleResult("INVALID"))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/games/INVALID/battles/result"))
            .andExpect(status().isNotFound());
    }
}