// src/test/java/ch/uzh/ifi/hase/soprafs26/service/GameSessionServiceTest.java
package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

class GameSessionServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private GameSessionService gameSessionService;

    private GameSession gameSession;
    private Player player;
    private User user;
    private User otherUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        gameSession = new GameSession();
        gameSession.setGameCode("ABC123");
        gameSession.setGameStatus(GameStatus.CONFIGURING);
        gameSession.setPlayer1Id(1L);
        gameSession.setPlayer2Id(2L);

        player = new Player();
        player.setUserId(1L);

        user = new User();
        user.setId(1L);
        user.setToken("valid-token");

        otherUser = new User();
        otherUser.setId(99L);
        otherUser.setToken("other-token");
    }

    @Test
    void createGameSession_validInput_setsDefaultsAndSaves() {
        GameSession input = new GameSession();
        input.setPlayer1Id(67L);
        WeatherGetDTO weather = new WeatherGetDTO();
        weather.setRainCategory(RainCategory.CLEAR);
        weather.setTemperatureCategory(TemperatureCategory.NEUTRAL);
        when(weatherService.getWeatherForLocation(any())).thenReturn(weather);

        when(gameSessionRepository.existsByGameCode(any())).thenReturn(false);
        when(gameSessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> {
            GameSession gs = invocation.getArgument(0);
            gs.setId(1L);
            return gs;
        });

        User user = new User();
        user.setId(67L);
        when(userRepository.findById(67L)).thenReturn(Optional.of(user));

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
        User mockUser = new User();
        mockUser.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockUser));

        GameSession result = gameSessionService.joinGameSession("ABC123", 2L);

        assertEquals(2L, result.getPlayer2Id());
        assertEquals(GameStatus.CONFIGURING, result.getGameStatus());
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
        verify(gameSessionRepository, times(1)).flush();
    }

    @Test
    void joinGameSession_invalidCodeFormat_throwsNotFound() {
        when(gameSessionRepository.findByGameCode("AB")).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession("AB", 2L));
        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void joinGameSession_nullCode_throwsNotFound() {
        when(gameSessionRepository.findByGameCode(null)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession(null, 2L));
        assertEquals(404, ex.getStatusCode().value());
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


    @Test
    public void saveWizardClass_validInput_savesWizardAndHp() {
        // given
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);
        given(userRepository.findByToken("valid-token")).willReturn(user);   
        given(playerRepository.findByUserId(1L)).willReturn(player);
        given(playerRepository.save(any(Player.class))).willAnswer(i -> i.getArgument(0));

        // when
        Player result = gameSessionService.saveWizardClass("ABC123", "valid-token", "ATTACKWIZARD");

        // then
        assertEquals(WizardClass.ATTACKWIZARD, result.getWizardClass());
        assertEquals((int)(100 * WizardClass.ATTACKWIZARD.getHpMultiplier()), result.getHp());
    }

    @Test
    public void saveWizardClass_gamblerWizard_hpWithinExpectedRange() {
        // given
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);        
        given(userRepository.findByToken("valid-token")).willReturn(user);  
        given(playerRepository.findByUserId(1L)).willReturn(player);
        given(playerRepository.save(any(Player.class))).willAnswer(i -> i.getArgument(0));

        // when
        Player result = gameSessionService.saveWizardClass("ABC123", "valid-token", "GAMBLERWIZARD");

        // then
        assertEquals(WizardClass.GAMBLERWIZARD, result.getWizardClass());
        assertTrue(result.getHp() >= 50);   // 100 * 0.5
        assertTrue(result.getHp() <= 150);  // 100 * 1.5
    }

    @Test
    public void saveWizardClass_invalidGameCode_throwsNotFound() {
        // given
        given(gameSessionRepository.findByGameCode("WRONG")).willReturn(null);

        // when/then
        assertThrows(ResponseStatusException.class, () ->
            gameSessionService.saveWizardClass("WRONG", "valid-token", "ATTACKWIZARD"));
    }

    @Test
    public void saveWizardClass_userNotInGame_throwsForbidden() {
        // given
        given(userRepository.findByToken("other-token")).willReturn(otherUser);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.saveWizardClass("ABC123", "other-token", "ATTACKWIZARD"));

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void saveWizardClass_gameNotInConfiguringStatus_throwsForbidden() {
        // given
        gameSession.setGameStatus(GameStatus.WAITING);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.saveWizardClass("ABC123", "valid-token", "ATTACKWIZARD"));

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    public void saveWizardClass_invalidWizardClassName_throwsBadRequest() {
        // given
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);
        given(userRepository.findByToken("valid-token")).willReturn(user);
        given(playerRepository.findByUserId(1L)).willReturn(player);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.saveWizardClass("ABC123", "valid-token", "INVALIDCLASS"));

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}