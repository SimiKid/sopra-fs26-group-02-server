package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LocationGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
class LocationControllerTest {

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
    void getLocationForGame_validCode_returnsLocationName() throws Exception {
        LocationGetDTO locationDTO = new LocationGetDTO();
        locationDTO.setLocationName("ZURICH");
        given(gameSessionService.getLocationDTOByCode("ABC123")).willReturn(locationDTO);

        mockMvc.perform(get("/games/ABC123/location")
            .header("Authorization", "test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locationName").value("ZURICH"));

        verify(gameSessionService).getLocationDTOByCode("ABC123");
    }

    @Test
    void getLocationForGame_unknownCode_returnsNotFound() throws Exception {
        given(gameSessionService.getLocationDTOByCode("UNKNOWN"))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        mockMvc.perform(get("/games/UNKNOWN/location")
            .header("Authorization", "test-token"))
            .andExpect(status().isNotFound());
    }
}
