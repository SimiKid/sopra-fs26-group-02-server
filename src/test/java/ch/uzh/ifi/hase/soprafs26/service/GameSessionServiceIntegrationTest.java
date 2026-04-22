package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Exercises GameSessionService end-to-end against H2. Focus: the lifecycle
 * state machine (WAITING -> CONFIGURING), the Player rows created as a
 * side-effect of create/join, and the currentGameSessionId bookkeeping
 * on both users — multi-repo interactions that unit-level mocks can hide.
 * WeatherService is mocked so tests don't hit the OpenWeather API.
 */
@SpringBootTest
@Transactional
public class GameSessionServiceIntegrationTest {

    @Autowired private GameSessionService gameSessionService;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayerRepository playerRepository;

    @MockitoBean private WeatherService weatherService;

    @BeforeEach
    public void stubWeather() {
        WeatherGetDTO w = new WeatherGetDTO();
        w.setRainCategory(RainCategory.CLEAR);
        w.setTemperatureCategory(TemperatureCategory.NEUTRAL);
        when(weatherService.getWeatherForLocation(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(w);
    }

    @Test
    public void createGameSession_persistsWaitingStateAndPlayer1() {
        User p1 = createUser("p1");

        GameSession saved = gameSessionService.createGameSession(sessionFor(p1));

        assertNotNull(saved.getId());
        assertNotNull(saved.getGameCode());
        assertEquals(6, saved.getGameCode().length());
        assertEquals(GameStatus.WAITING, saved.getGameStatus());
        assertEquals(p1.getId(), saved.getActivePlayerId());
        assertEquals(RainCategory.CLEAR, saved.getRain());
        assertEquals(TemperatureCategory.NEUTRAL, saved.getTemperature());

        Player player1 = playerRepository.findByUserIdAndGameSessionId(p1.getId(), saved.getId());
        assertNotNull(player1);
        assertFalse(player1.isReady());

        User reloaded = userRepository.findById(p1.getId()).orElseThrow();
        assertEquals(saved.getId(), reloaded.getCurrentGameSessionId());
    }

    @Test
    public void createGameSession_userAlreadyInSession_conflict() {
        User p1 = createUser("p1");
        gameSessionService.createGameSession(sessionFor(p1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.createGameSession(sessionFor(p1)));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void joinGameSession_transitionsToConfiguringAndCreatesPlayer2() {
        User p1 = createUser("p1");
        User p2 = createUser("p2");
        GameSession created = gameSessionService.createGameSession(sessionFor(p1));

        GameSession joined = gameSessionService.joinGameSession(created.getGameCode(), p2.getId());

        assertEquals(GameStatus.CONFIGURING, joined.getGameStatus());
        assertEquals(p2.getId(), joined.getPlayer2Id());

        Player player2 = playerRepository.findByUserIdAndGameSessionId(p2.getId(), joined.getId());
        assertNotNull(player2);

        User p2Reloaded = userRepository.findById(p2.getId()).orElseThrow();
        assertEquals(joined.getId(), p2Reloaded.getCurrentGameSessionId());
    }

    @Test
    public void joinGameSession_ownGame_conflict() {
        User p1 = createUser("p1");
        GameSession created = gameSessionService.createGameSession(sessionFor(p1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession(created.getGameCode(), p1.getId()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void joinGameSession_unknownCode_notFound() {
        User p2 = createUser("p2");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.joinGameSession("ZZZZZZ", p2.getId()));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void saveWizardClass_setsClassAndHpFromMultiplier() {
        User p1 = createUser("p1");
        User p2 = createUser("p2");
        GameSession created = gameSessionService.createGameSession(sessionFor(p1));
        gameSessionService.joinGameSession(created.getGameCode(), p2.getId());

        Player updated = gameSessionService.saveWizardClass(
                created.getGameCode(), p1.getToken(), WizardClass.ATTACKWIZARD.name());

        assertEquals(WizardClass.ATTACKWIZARD, updated.getWizardClass());
        assertEquals(80, updated.getHp()); // 100 * 0.8
    }

    @Test
    public void saveWizardClass_beforeJoin_forbiddenBecauseStillWaiting() {
        User p1 = createUser("p1");
        GameSession created = gameSessionService.createGameSession(sessionFor(p1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> gameSessionService.saveWizardClass(
                        created.getGameCode(), p1.getToken(), WizardClass.TANKWIZARD.name()));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // -- helpers --

    private User createUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("pw");
        return userService.createUser(u);
    }

    private GameSession sessionFor(User p1) {
        GameSession s = new GameSession();
        s.setPlayer1Id(p1.getId());
        return s;
    }
}
