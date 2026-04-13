package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.Interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameSessionController.class)
public class GameSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
        private AuthInterceptor authInterceptor;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private GameSessionService gameSessionService;

    @BeforeEach
    void setup() {
        Mockito.when(authInterceptor.preHandle(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(true);
    }

    @Test
    public void createGameSession_validToken_gameCreated() throws Exception {
        // given
        User creator = new User();
        creator.setId(1L);
        creator.setToken("valid-token");

        GameSession createdGameSession = new GameSession();
        createdGameSession.setId(10L);
        createdGameSession.setGameCode("ABC123");
        createdGameSession.setGameStatus(GameStatus.WAITING);
        createdGameSession.setPlayer1Id(1L);
        createdGameSession.setPlayer2Id(null);
        createdGameSession.setActivePlayerId(1L);
        createdGameSession.setCreatedAt(LocalDateTime.of(2026, 4, 8, 10, 0));

        given(authenticationService.authenticateByToken("valid-token")).willReturn(creator);
        given(gameSessionService.createGameSession(Mockito.any(GameSession.class))).willReturn(createdGameSession);

        // when
        MockHttpServletRequestBuilder postRequest = post("/games")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(createdGameSession.getId().intValue())))
            .andExpect(jsonPath("$.gameCode", is(createdGameSession.getGameCode())))
            .andExpect(jsonPath("$.gameStatus", is(createdGameSession.getGameStatus().toString())))
            .andExpect(jsonPath("$.player1Id", is(createdGameSession.getPlayer1Id().intValue())))
            .andExpect(jsonPath("$.activePlayerId", is(createdGameSession.getActivePlayerId().intValue())));

        ArgumentCaptor<GameSession> gameSessionCaptor = ArgumentCaptor.forClass(GameSession.class);
        verify(gameSessionService).createGameSession(gameSessionCaptor.capture());
        assert gameSessionCaptor.getValue().getPlayer1Id().equals(creator.getId());
    }

    @Test
    public void createGameSession_invalidToken_unauthorized() throws Exception {
        // given
        given(authenticationService.authenticateByToken("invalid-token"))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));

        // when
        MockHttpServletRequestBuilder postRequest = post("/games")
            .header("Authorization", "invalid-token");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isUnauthorized());

        verify(gameSessionService, never()).createGameSession(Mockito.any(GameSession.class));
    }

    @Test
    public void getGameSessionByCode_validInput_returnsGameSession() throws Exception {
        // given
        User requester = new User();
        requester.setId(2L);
        requester.setToken("valid-token");

        GameSession gameSession = new GameSession();
        gameSession.setId(20L);
        gameSession.setGameCode("ZXCVBN");
        gameSession.setGameStatus(GameStatus.BATTLE);
        gameSession.setPlayer1Id(1L);
        gameSession.setPlayer2Id(2L);
        gameSession.setActivePlayerId(2L);
        gameSession.setCreatedAt(LocalDateTime.of(2026, 4, 8, 9, 30));

        given(gameSessionService.getByGameCode("ZXCVBN")).willReturn(gameSession);

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/ZXCVBN")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(gameSession.getId().intValue())))
            .andExpect(jsonPath("$.gameCode", is(gameSession.getGameCode())))
            .andExpect(jsonPath("$.gameStatus", is(gameSession.getGameStatus().toString())))
            .andExpect(jsonPath("$.player1Id", is(gameSession.getPlayer1Id().intValue())))
            .andExpect(jsonPath("$.player2Id", is(gameSession.getPlayer2Id().intValue())))
            .andExpect(jsonPath("$.activePlayerId", is(gameSession.getActivePlayerId().intValue())));
    }

    @Test
    public void deleteGameSession_existingGame_noContent() throws Exception {
        // given
        User requester = new User();
        requester.setId(2L);
        requester.setToken("valid-token");

        doNothing().when(gameSessionService).deleteByGameCode("DEL123");

        // when
        MockHttpServletRequestBuilder deleteRequest = delete("/games/DEL123")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(deleteRequest)
            .andExpect(status().isNoContent());
    }

    @Test
    public void deleteGameSession_missingGame_notFound() throws Exception {
        // given
        User requester = new User();
        requester.setId(2L);
        requester.setToken("valid-token");

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found"))
            .when(gameSessionService).deleteByGameCode("UNKNOWN");

        // when
        MockHttpServletRequestBuilder deleteRequest = delete("/games/UNKNOWN")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(deleteRequest)
            .andExpect(status().isNotFound());
    }

    @Test
    public void createGameSession_missingAuthorizationHeader_badRequest() throws Exception {
        // when
        MockHttpServletRequestBuilder postRequest = post("/games");

        // then
        mockMvc.perform(postRequest)
            .andExpect(status().isBadRequest());

        verify(gameSessionService, never()).createGameSession(Mockito.any(GameSession.class));
    }

    @Test
    public void getGameSessionByCode_notFound_notFound() throws Exception {
        // given
        User requester = new User();
        requester.setId(2L);
        requester.setToken("valid-token");

        given(authenticationService.authenticateByToken("valid-token")).willReturn(requester);
        given(gameSessionService.getByGameCode("UNKNOWN"))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/games/UNKNOWN")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isNotFound());
    }


    @Test
    public void joinGameSession_validToken_gameJoined() throws Exception {
        // given
        User joiner = new User();
        joiner.setId(2L);
        joiner.setToken("valid-token");

        GameSession joinedGame = new GameSession();
        joinedGame.setId(10L);
        joinedGame.setGameCode("ABC123");
        joinedGame.setGameStatus(GameStatus.CONFIGURING);
        joinedGame.setPlayer1Id(1L);
        joinedGame.setPlayer2Id(2L);
        joinedGame.setActivePlayerId(1L);
        joinedGame.setCreatedAt(LocalDateTime.of(2026, 4, 8, 10, 0));

        given(authenticationService.authenticateByToken("valid-token")).willReturn(joiner);
        given(gameSessionService.joinGameSession("ABC123", 2L)).willReturn(joinedGame);

        // when
        MockHttpServletRequestBuilder putRequest = put("/games/ABC123/join")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(putRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(joinedGame.getId().intValue())))
            .andExpect(jsonPath("$.gameCode", is("ABC123")))
            .andExpect(jsonPath("$.gameStatus", is("CONFIGURING")))
            .andExpect(jsonPath("$.player1Id", is(1)))
            .andExpect(jsonPath("$.player2Id", is(2)))
            .andExpect(jsonPath("$.activePlayerId", is(1)));
    }

    @Test
    public void joinGameSession_invalidToken_unauthorized() throws Exception {
        // given
        given(authenticationService.authenticateByToken("invalid-token"))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token"));

        // when
        MockHttpServletRequestBuilder putRequest = put("/games/ABC123/join")
            .header("Authorization", "invalid-token");

        // then
        mockMvc.perform(putRequest)
            .andExpect(status().isUnauthorized());

        verify(gameSessionService, never()).joinGameSession(Mockito.anyString(), Mockito.anyLong());
    }

    @Test
    public void joinGameSession_missingAuthorizationHeader_badRequest() throws Exception {
        // when
        MockHttpServletRequestBuilder putRequest = put("/games/ABC123/join");

        // then
        mockMvc.perform(putRequest)
            .andExpect(status().isBadRequest());

        verify(gameSessionService, never()).joinGameSession(Mockito.anyString(), Mockito.anyLong());
    }

    @Test
    public void joinGameSession_gameNotFound_notFound() throws Exception {
        // given
        User joiner = new User();
        joiner.setId(2L);
        joiner.setToken("valid-token");

        given(authenticationService.authenticateByToken("valid-token")).willReturn(joiner);
        given(gameSessionService.joinGameSession("NOTFND", 2L))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or expired."));

        // when
        MockHttpServletRequestBuilder putRequest = put("/games/NOTFND/join")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(putRequest)
            .andExpect(status().isNotFound());
    }

    @Test
    public void joinGameSession_gameFull_conflict() throws Exception {
        // given
        User joiner = new User();
        joiner.setId(2L);
        joiner.setToken("valid-token");

        given(authenticationService.authenticateByToken("valid-token")).willReturn(joiner);
        given(gameSessionService.joinGameSession("ABC123", 2L))
            .willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Game is already full."));

        // when
        MockHttpServletRequestBuilder putRequest = put("/games/ABC123/join")
            .header("Authorization", "valid-token");

        // then
        mockMvc.perform(putRequest)
            .andExpect(status().isConflict());
    }
}