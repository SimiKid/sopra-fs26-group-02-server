package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import ch.uzh.ifi.hase.soprafs26.service.MatchMakingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchMakingControllerTest {

    @Mock
    private MatchMakingService matchMakingService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private MatchMakingController matchMakingController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        matchMakingController = new MatchMakingController(matchMakingService, authenticationService);
        mockMvc = MockMvcBuilders.standaloneSetup(matchMakingController).build();
    }

    @Test
    void joinRandomGameSession_ReturnsOk_IfAuthenticated() throws Exception {
        // given
        String token = "validToken";
        Long userId = 99L;
        User user = new User();
        user.setId(userId);

        when(authenticationService.authenticateByToken(token)).thenReturn(user);

        // when/then
        mockMvc.perform(post("/matchmaking/join")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(authenticationService, times(1)).authenticateByToken(token);
        verify(matchMakingService, times(1)).joinRandomGameSession(userId);
    }

    @Test
    void joinRandomGameSession_Returns401_IfNotAuthenticated() throws Exception {
        // given
        String token = "invalidToken";
        when(authenticationService.authenticateByToken(token)).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        // when/then
        mockMvc.perform(post("/matchmaking/join")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(authenticationService, times(1)).authenticateByToken(token);
        verify(matchMakingService, never()).joinRandomGameSession(any());
    }

    @Test
    void leaveGameSession_ReturnsOk_IfAuthenticated() throws Exception {
        // given
        String token = "someToken";
        Long userId = 23L;
        User user = new User();
        user.setId(userId);

        when(authenticationService.authenticateByToken(token)).thenReturn(user);

        // when/then
        mockMvc.perform(delete("/matchmaking/leave")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(authenticationService, times(1)).authenticateByToken(token);
        verify(matchMakingService, times(1)).leaveGameSession(userId);
    }

    @Test
    void leaveGameSession_Returns401_IfNotAuthenticated() throws Exception {
        // given
        String token = "badToken";
        when(authenticationService.authenticateByToken(token)).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        // when/then
        mockMvc.perform(delete("/matchmaking/leave")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(authenticationService, times(1)).authenticateByToken(token);
        verify(matchMakingService, never()).leaveGameSession(any());
    }
}

