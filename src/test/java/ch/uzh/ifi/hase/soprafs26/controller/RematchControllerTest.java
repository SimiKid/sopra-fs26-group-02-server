package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.service.RematchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RematchController.class)
class RematchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthInterceptor authInterceptor;

    @MockitoBean
    private RematchService rematchService;

    @BeforeEach
    void setup() {
        Mockito.when(authInterceptor.preHandle(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(true);
    }

    @Test
    void requestRematch_validToken_returnsOkAndDelegatesToService() throws Exception {
        mockMvc.perform(post("/games/ABC123/rematch").header("Authorization", "tok"))
            .andExpect(status().isOk());

        verify(rematchService).requestRematch("ABC123", "tok");
    }

    @Test
    void requestRematch_gameNotFound_returnsNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"))
            .when(rematchService).requestRematch("XXXXXX", "tok");

        mockMvc.perform(post("/games/XXXXXX/rematch").header("Authorization", "tok"))
            .andExpect(status().isNotFound());
    }

    @Test
    void requestRematch_gameNotFinished_returnsConflict() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Game not finished"))
            .when(rematchService).requestRematch("ABC123", "tok");

        mockMvc.perform(post("/games/ABC123/rematch").header("Authorization", "tok"))
            .andExpect(status().isConflict());
    }

    @Test
    void requestRematch_notParticipant_returnsForbidden() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant"))
            .when(rematchService).requestRematch("ABC123", "tok-outsider");

        mockMvc.perform(post("/games/ABC123/rematch").header("Authorization", "tok-outsider"))
            .andExpect(status().isForbidden());
    }
}
