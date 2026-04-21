package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.BattleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleResultGetDTO;

public class BattleServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BattleRepository battleRepository;

    @InjectMocks
    private BattleService battleService;

    private GameSession session;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        session = new GameSession();
        session.setId(1L);
        session.setGameCode("ABC123");
        session.setPlayer1Id(1L);
        session.setPlayer2Id(2L);
        session.setWinnerId(1L);
        session.setGameStatus(GameStatus.FINISHED);
        session.setTemperature(TemperatureCategory.HOT);
        session.setRain(RainCategory.CLEAR);
    }

    @Test
    void getBattleResult_success_returnsCorrectDTO() {
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);
        given(battleRepository.sumDamageByGameId(1L)).willReturn(150);
        given(battleRepository.countTurnsByGameId(1L)).willReturn(5);

        BattleResultGetDTO result = battleService.getBattleResult("ABC123");

        assertEquals(1L, result.getWinnerUserId());
        assertEquals(2L, result.getLoserUserId());
        assertEquals(150, result.getTotalDamageDealt());
        assertEquals(5, result.getTurnsPlayed());
        assertEquals(RainCategory.CLEAR, result.getWeather().getRainCategory());
        assertEquals(TemperatureCategory.HOT, result.getWeather().getTemperatureCategory());
    }

    @Test
    void getBattleResult_gameNotFound_throws404() {
        given(gameSessionRepository.findByGameCode("INVALID")).willReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> battleService.getBattleResult("INVALID"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getBattleResult_gameNotFinished_throws404() {
        session.setGameStatus(GameStatus.BATTLE);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> battleService.getBattleResult("ABC123"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getBattleResult_nullDamage_returnsTotalDamageZero() {
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);
        given(battleRepository.sumDamageByGameId(1L)).willReturn(null);
        given(battleRepository.countTurnsByGameId(1L)).willReturn(0);

        BattleResultGetDTO result = battleService.getBattleResult("ABC123");

        assertEquals(0, result.getTotalDamageDealt());
    }
}