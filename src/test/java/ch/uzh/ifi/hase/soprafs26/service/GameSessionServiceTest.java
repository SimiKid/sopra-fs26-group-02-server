// src/test/java/ch/uzh/ifi/hase/soprafs26/service/GameSessionServiceTest.java
package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameSessionServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @InjectMocks
    private GameSessionService gameSessionService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createGameSession_validInput_setsDefaultsAndSaves() {
        GameSession input = new GameSession();
        input.setPlayer1Id(67L);

        when(gameSessionRepository.existsByGameCode(any())).thenReturn(false);
        when(gameSessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> {
            GameSession gs = invocation.getArgument(0);
            gs.setId(1L);
            return gs;
        });

        GameSession created = gameSessionService.createGameSession(input);

        assertNotNull(created.getId());
        assertNotNull(created.getGameCode());
        assertEquals(6, created.getGameCode().length());
        assertEquals(GameStatus.WAITING, created.getGameStatus());
        assertEquals(67L, created.getPlayer1Id());
        assertNotNull(created.getCreatedAt());

        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
        verify(gameSessionRepository, times(1)).flush();
    }

    @Test
    void getByGameCode_existing_returnsGame() {
        GameSession existing = new GameSession();
        existing.setGameCode("ABC123");

        when(gameSessionRepository.findByGameCode("ABC123")).thenReturn(existing);

        GameSession found = gameSessionService.getByGameCode("ABC123");
        assertEquals("ABC123", found.getGameCode());
    }

    @Test
    void getByGameCode_missing_throwsNotFound() {
        when(gameSessionRepository.findByGameCode("MISSING")).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> gameSessionService.getByGameCode("MISSING"));
    }

    @Test
    void cleanupExpiredGameSessions_withExpiredWaitingSessions_deletesEachExpiredSession() {
        GameSession expiredOne = new GameSession();
        expiredOne.setGameCode("OLD001");
        expiredOne.setCreatedAt(LocalDateTime.now().minusMinutes(15));

        GameSession expiredTwo = new GameSession();
        expiredTwo.setGameCode("OLD002");
        expiredTwo.setCreatedAt(LocalDateTime.now().minusMinutes(12));

        when(gameSessionRepository.findByPlayer2IdIsNullAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expiredOne, expiredTwo));

        gameSessionService.cleanupExpiredGameSessions();

        verify(gameSessionRepository, times(1))
                .findByPlayer2IdIsNullAndCreatedAtBefore(any(LocalDateTime.class));
        verify(gameSessionRepository, times(1)).delete(expiredOne);
        verify(gameSessionRepository, times(1)).delete(expiredTwo);
    }

    @Test
    void joinGameSession_validInput_setsPlayer2AndStatusConfiguring() {
        GameSession existing = new GameSession();
        existing.setId(1L);
        existing.setGameCode("ABC123");
        existing.setGameStatus(GameStatus.WAITING);
        existing.setPlayer1Id(1L);
        existing.setPlayer2Id(null);

        when(gameSessionRepository.findByGameCode("ABC123")).thenReturn(existing);
        when(gameSessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameSession result = gameSessionService.joinGameSession("ABC123", 2L);

        assertEquals(2L, result.getPlayer2Id());
        assertEquals(GameStatus.CONFIGURING, result.getGameStatus());
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
        verify(gameSessionRepository, times(1)).flush();
    }

    @Test
    void joinGameSession_invalidCodeFormat_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession("AB", 2L));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void joinGameSession_nullCode_throwsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession(null, 2L));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void joinGameSession_gameNotFound_throwsNotFound() {
        when(gameSessionRepository.findByGameCode("NOTFND")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession("NOTFND", 2L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void joinGameSession_gameNotWaiting_throwsConflict() {
        GameSession existing = new GameSession();
        existing.setGameCode("ABC123");
        existing.setGameStatus(GameStatus.BATTLE);
        existing.setPlayer1Id(1L);

        when(gameSessionRepository.findByGameCode("ABC123")).thenReturn(existing);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession("ABC123", 2L));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void joinGameSession_gameAlreadyFull_throwsConflict() {
        GameSession existing = new GameSession();
        existing.setGameCode("ABC123");
        existing.setGameStatus(GameStatus.WAITING);
        existing.setPlayer1Id(1L);
        existing.setPlayer2Id(3L);

        when(gameSessionRepository.findByGameCode("ABC123")).thenReturn(existing);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession("ABC123", 2L));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void joinGameSession_joiningOwnGame_throwsConflict() {
        GameSession existing = new GameSession();
        existing.setGameCode("ABC123");
        existing.setGameStatus(GameStatus.WAITING);
        existing.setPlayer1Id(1L);
        existing.setPlayer2Id(null);

        when(gameSessionRepository.findByGameCode("ABC123")).thenReturn(existing);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession("ABC123", 1L));
        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void cleanupExpiredGameSessions_withNoExpiredSessions_doesNotDeleteAnything() {
        when(gameSessionRepository.findByPlayer2IdIsNullAndCreatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        gameSessionService.cleanupExpiredGameSessions();

        verify(gameSessionRepository, times(1))
                .findByPlayer2IdIsNullAndCreatedAtBefore(any(LocalDateTime.class));
        verify(gameSessionRepository, never()).delete(any(GameSession.class));
    }
}