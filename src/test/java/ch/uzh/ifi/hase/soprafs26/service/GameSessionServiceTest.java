// src/test/java/ch/uzh/ifi/hase/soprafs26/service/GameSessionServiceTest.java
package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.constant.Location;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

    @Mock
    private SimpMessagingTemplate messagingTemplate;

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
        gameSession.setId(1L);
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
        when(weatherService.getWeatherForLocation(any(GameSession.class), any(Location.class))).thenReturn(weather);
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
    void createGameSession_savesPlayerLinkedToSession() {
        GameSession input = new GameSession();
        input.setPlayer1Id(67L);
        WeatherGetDTO weather = new WeatherGetDTO();
        weather.setRainCategory(RainCategory.CLEAR);
        weather.setTemperatureCategory(TemperatureCategory.NEUTRAL);
        when(weatherService.getWeatherForLocation(any(GameSession.class), any(Location.class))).thenReturn(weather);

        when(gameSessionRepository.existsByGameCode(any())).thenReturn(false);
        when(gameSessionRepository.save(any(GameSession.class))).thenAnswer(invocation -> {
            GameSession gs = invocation.getArgument(0);
            gs.setId(42L);
            return gs;
        });

        User user = new User();
        user.setId(67L);
        when(userRepository.findById(67L)).thenReturn(Optional.of(user));

        gameSessionService.createGameSession(input);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository).save(playerCaptor.capture());
        Player saved = playerCaptor.getValue();
        assertEquals(67L, saved.getUserId());
        assertEquals(42L, saved.getGameSessionId());
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
        given(playerRepository.findByUserIdAndGameSessionId(1L, 1L)).willReturn(player);
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
        given(playerRepository.findByUserIdAndGameSessionId(1L, 1L)).willReturn(player);
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
        given(playerRepository.findByUserIdAndGameSessionId(1L, 1L)).willReturn(player);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.saveWizardClass("ABC123", "valid-token", "INVALIDCLASS"));

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void getBattleCount_returnsCountFromRepository() {
        given(gameSessionRepository.countStartedBattles()).willReturn(42L);

        long count = gameSessionService.getBattleCount();

        assertEquals(42L, count);
}
    @Test
    void leaveGameSession_configuring_phase_removesPlayersAndSession_andSendsMessage() {
        // given
        gameSession.setGameStatus(GameStatus.CONFIGURING);
        gameSession.setPlayer1Id(user.getId());
        gameSession.setPlayer2Id(null);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);
        given(userRepository.findByToken("valid-token")).willReturn(user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(playerRepository.findByGameSessionId(gameSession.getId())).willReturn(List.of(player));

        // when
        gameSessionService.leaveGameSession("ABC123", "valid-token");

        // then
        verify(userRepository, times(1)).save(any(User.class)); // user id nullified
        verify(playerRepository, times(1)).delete(player);
        verify(gameSessionRepository, times(1)).delete(gameSession);
        verify(messagingTemplate, times(1)).convertAndSend("/topic/game/ABC123/player-left", "PLAYER_LEFT");
    }

    @Test
    void leaveGameSession_battle_phase_updatesStatsAndStatus_andSendsBattleEnded() {
        // given
        User player1 = new User();
        User player2 = new User();
        player1.setId(1L);
        player2.setId(2L);
        player1.setToken("token1");
        player2.setToken("token2");
        player1.setWins(2);
        player1.setTotalGames(4);
        player1.setLosses(2);
        player2.setWins(3);
        player2.setTotalGames(5);
        player2.setLosses(2);

        GameSession session = new GameSession();
        session.setId(99L);
        session.setGameCode("CODE99");
        session.setPlayer1Id(player1.getId());
        session.setPlayer2Id(player2.getId());
        session.setGameStatus(GameStatus.BATTLE);

        given(gameSessionRepository.findByGameCode("CODE99")).willReturn(session);
        given(userRepository.findById(player1.getId())).willReturn(Optional.of(player1));
        given(userRepository.findById(player2.getId())).willReturn(Optional.of(player2));

        // when player1 leaves
        gameSessionService.leaveGameSession("CODE99", "token1");

        // then
        assertEquals(GameStatus.FINISHED, session.getGameStatus());
        assertEquals(3, player1.getLosses());
        assertEquals(5, player1.getTotalGames());
        assertEquals(2, player1.getWins());
        assertEquals(4, player2.getWins());
        assertEquals(6, player2.getTotalGames());
        // Check win rates have changed
        assertEquals((float) player1.getWins() / player1.getTotalGames(), player1.getWinRate());
        assertEquals((float) player2.getWins() / player2.getTotalGames(), player2.getWinRate());

        verify(userRepository, times(1)).save(player1);
        verify(userRepository, times(1)).save(player2);
        verify(messagingTemplate, times(1)).convertAndSend("/topic/game/CODE99/battle-ended", "PLAYER_LEFT_IN_BATTLE");
    }

    @Test
    void leaveGameSession_phaseNotAllowed_throwsForbidden() {
        // given
        gameSession.setGameStatus(GameStatus.FINISHED);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);

        // when/then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.leaveGameSession("ABC123", "valid-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void leaveGameSession_userNotPartOfGame_throwsForbidden_inConfiguring() {
        // given
        gameSession.setGameStatus(GameStatus.CONFIGURING);
        gameSession.setPlayer1Id(1L);
        gameSession.setPlayer2Id(2L);
        User nonparticipant = new User();
        nonparticipant.setId(3L);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);
        given(userRepository.findByToken("non-token")).willReturn(nonparticipant);

        // when/then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.leaveGameSession("ABC123", "non-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void leaveGameSession_userNotPartOfGame_throwsForbidden_inBattle() {
        // given
        gameSession.setGameStatus(GameStatus.BATTLE);
        gameSession.setPlayer1Id(1L);
        gameSession.setPlayer2Id(2L);
        gameSession.setId(21L);

        User u1 = new User();
        u1.setId(1L);
        u1.setToken("token1");
        User u2 = new User();
        u2.setId(2L);
        u2.setToken("token2");
        User nonparticipant = new User();
        nonparticipant.setId(3L);
        nonparticipant.setToken("other-token");

        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);
        given(userRepository.findById(1L)).willReturn(Optional.of(u1));
        given(userRepository.findById(2L)).willReturn(Optional.of(u2));

        // when/then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.leaveGameSession("ABC123", "other-token"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }
    @Test
    void getPlayerStatusandgiveMessage_returnsPlayerReadyStatus_andSendsMessage() {
        // given
        gameSession.setGameCode("ABC123");
        gameSession.setId(101L);

        User user = new User();
        user.setId(11L);
        user.setToken("token-test");
        Player player = new Player();
        player.setId(100L);
        player.setUserId(11L);
        player.setGameSessionId(101L);
        player.setReady(true);

        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);
        given(userRepository.findByToken("token-test")).willReturn(user);
        given(playerRepository.findByUserIdAndGameSessionId(11L, 101L)).willReturn(player);

        // when
        boolean result = gameSessionService.getPlayerStatusandgiveMessage("ABC123", "token-test");

        // then
        assertTrue(result);
        verify(messagingTemplate).convertAndSend("/topic/game/ABC123/player-status", "TIME_EXPIRED");
    }

    @Test
    void getPlayerStatusandgiveMessage_userNotPartOfGame_throwsForbidden() {
        // given
        gameSession.setGameCode("ABC123");
        gameSession.setId(101L);
        User user = new User();
        user.setId(2L);
        user.setToken("token2");

        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(gameSession);
        given(userRepository.findByToken("token2")).willReturn(user);
        given(playerRepository.findByUserIdAndGameSessionId(2L, 101L)).willReturn(null);

        // when/then
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            gameSessionService.getPlayerStatusandgiveMessage("ABC123", "token2"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

}