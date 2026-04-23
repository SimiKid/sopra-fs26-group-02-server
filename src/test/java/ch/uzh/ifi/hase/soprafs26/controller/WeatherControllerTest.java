package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @MockitoBean
    private GameSessionService gameSessionService;

    @BeforeEach
    void setup() {
        Mockito.when(authInterceptor.preHandle(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(true);
    }

    @Test
    void getWeatherForGame_validCode_returnsWeatherDto() throws Exception {
        WeatherGetDTO dto = new WeatherGetDTO();
        dto.setRainCategory(RainCategory.RAINING);
        dto.setTemperatureCategory(TemperatureCategory.HOT);

        given(gameSessionService.getWeatherByCode("ABC123")).willReturn(dto);

        mockMvc.perform(get("/games/ABC123/weather")
                .header("Authorization", "test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rainCategory", is("RAINING")))
            .andExpect(jsonPath("$.temperatureCategory", is("HOT")));

        verify(gameSessionService).getWeatherByCode("ABC123");
    }

    @Test
    void getWeatherForGame_unknownCode_returnsNotFound() throws Exception {
        given(gameSessionService.getWeatherByCode("UNKNOWN"))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        mockMvc.perform(get("/games/UNKNOWN/weather")
                .header("Authorization", "test-token"))
            .andExpect(status().isNotFound());
    }
}
