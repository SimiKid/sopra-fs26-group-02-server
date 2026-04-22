package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RematchServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private GameSessionService gameSessionService;
    @Mock
    private UserRepository userRepository; 

    @InjectMocks
    private RematchService rematchService;

    private GameSession session;
    private User player1;
    private User player2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        session = new GameSession();
        session.setGameCode("ABC123");
        session.setGameStatus(GameStatus.FINISHED);
        session.setPlayer1Id(1L);
        session.setPlayer2Id(2L);
        session.setPlayer1WantsRematch(false);
        session.setPlayer2WantsRematch(false);

        player1 = new User();
        player1.setId(1L);
        player1.setToken("token-p1");

        player2 = new User();
        player2.setId(2L);
        player2.setToken("token-p2");
    }

    @Test
    void requestRematch_firstPlayerAccepts_onlyFlagSet() {
        // given
        given(authenticationService.authenticateByToken("token-p1")).willReturn(player1);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);
        given(gameSessionRepository.save(any(GameSession.class))).willAnswer(i -> i.getArgument(0));

        // when
        rematchService.requestRematch("ABC123", "token-p1");

        // then — flag set, but no new game created yet
        assertTrue(session.getPlayer1WantsRematch());
        assertFalse(session.getPlayer2WantsRematch());
        verify(gameSessionService, never()).createGameSession(any());
    }

    @Test
    void requestRematch_bothPlayersAccept_newGameCreated() {
        // given — player1 already accepted
        session.setPlayer1WantsRematch(true);

        GameSession newSession = new GameSession();
        newSession.setGameCode("XYZ789");

        player1.setCurrentGameSessionId(session.getId());
        player2.setCurrentGameSessionId(session.getId());

        given(authenticationService.authenticateByToken("token-p2")).willReturn(player2);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session); // both calls return same session
        given(gameSessionRepository.save(any(GameSession.class))).willAnswer(i -> i.getArgument(0));
        given(userRepository.findById(1L)).willReturn(java.util.Optional.of(player1));
        given(userRepository.findById(2L)).willReturn(java.util.Optional.of(player2));
        given(gameSessionService.createGameSession(any())).willReturn(newSession);
        // when
        rematchService.requestRematch("ABC123", "token-p2");

        // then — new game created and code stored on old session
        verify(gameSessionService).createGameSession(any());
        verify(gameSessionService).joinGameSession("XYZ789", 2L);
        assertEquals("XYZ789", session.getRematchGameCode());
        assertFalse(session.getPlayer1WantsRematch());
        assertFalse(session.getPlayer2WantsRematch());
    }

    @Test
    void requestRematch_gameNotFound_throwsNotFound() {
        given(authenticationService.authenticateByToken("token-p1")).willReturn(player1);
        given(gameSessionRepository.findByGameCode("XXXXXX")).willReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> rematchService.requestRematch("XXXXXX", "token-p1"));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void requestRematch_gameNotFinished_throwsConflict() {
        session.setGameStatus(GameStatus.BATTLE);
        given(authenticationService.authenticateByToken("token-p1")).willReturn(player1);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> rematchService.requestRematch("ABC123", "token-p1"));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void requestRematch_nonParticipant_throwsForbidden() {
        User outsider = new User();
        outsider.setId(99L);
        outsider.setToken("token-outsider");

        given(authenticationService.authenticateByToken("token-outsider")).willReturn(outsider);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> rematchService.requestRematch("ABC123", "token-outsider"));
        assertEquals(403, ex.getStatusCode().value());
    }
}