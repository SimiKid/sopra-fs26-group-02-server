package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

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
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.Battle;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.BattleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleResultGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;

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

    @Test
    void resolveAttack_validTurn_reducesDefenderHpAndSwitchesTurn() {
        // odd turn count -> battle can't end; FIREBALL at NEUTRAL/CLEAR with BALANCED wizard
        // damage = 30 * 1.0 * 1.0 (NEUTRAL on FIRE) * 1.2 (CLEAR on FIRE) = 36
        primeBattle(TemperatureCategory.NEUTRAL, RainCategory.CLEAR, 1);
        User attackerUser = user(1L, "token-p1");
        User defenderUser = user(2L, "token-p2");
        Player attacker = player(1L, 100, WizardClass.BALANCEDWIZARD);
        Player defender = player(2L, 100, WizardClass.BALANCEDWIZARD);
        wireUsersAndPlayers(attackerUser, defenderUser, attacker, defender);

        battleService.resolveAttack("ABC123", "token-p1", "FIREBALL");

        assertEquals(64, defender.getHp());
        assertEquals(2L, session.getActivePlayerId());
        assertEquals(GameStatus.BATTLE, session.getGameStatus());
        assertNull(session.getWinnerId());
        verify(battleRepository).save(any(Battle.class));
        verify(playerRepository).save(defender);
    }

    @Test
    void resolveAttack_notActivePlayer_throwsForbidden() {
        session.setGameStatus(GameStatus.BATTLE);
        session.setActivePlayerId(2L); // player2 is active, but player1 attacks
        User attackerUser = user(1L, "token-p1");
        given(authenticationService.authenticateByToken("token-p1")).willReturn(attackerUser);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> battleService.resolveAttack("ABC123", "token-p1", "FIREBALL"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void resolveAttack_unknownGameCode_throws404() {
        given(gameSessionRepository.findByGameCode("INVALID")).willReturn(null);
        given(authenticationService.authenticateByToken("token-p1")).willReturn(user(1L, "token-p1"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> battleService.resolveAttack("INVALID", "token-p1", "FIREBALL"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void resolveAttack_gameNotInBattle_throws409() {
        session.setGameStatus(GameStatus.WAITING);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);
        given(authenticationService.authenticateByToken("token-p1")).willReturn(user(1L, "token-p1"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> battleService.resolveAttack("ABC123", "token-p1", "FIREBALL"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void resolveAttack_lethalHitOnEvenTurn_endsGameAndSetsWinner() {
        // even turn + defender drops to <=0 -> game ends; attacker has more HP, so attacker wins
        primeBattle(TemperatureCategory.NEUTRAL, RainCategory.CLEAR, 2);
        User attackerUser = user(1L, "token-p1");
        User defenderUser = user(2L, "token-p2");
        attackerUser.setCurrentGameSessionId(1L);
        defenderUser.setCurrentGameSessionId(1L);
        Player attacker = player(1L, 50, WizardClass.BALANCEDWIZARD);
        Player defender = player(2L, 10, WizardClass.BALANCEDWIZARD); // dies after 36 damage
        wireUsersAndPlayers(attackerUser, defenderUser, attacker, defender);

        battleService.resolveAttack("ABC123", "token-p1", "FIREBALL");

        assertEquals(GameStatus.FINISHED, session.getGameStatus());
        assertEquals(1L, session.getWinnerId());
        assertNull(attackerUser.getCurrentGameSessionId());
        assertNull(defenderUser.getCurrentGameSessionId());
    }

    @Test
    void resolveAttack_oddTurnWithLethalDamage_doesNotEndGame() {
        // key battle rule: on odd turns the losing player still gets a swing, so game continues
        primeBattle(TemperatureCategory.NEUTRAL, RainCategory.CLEAR, 1);
        User attackerUser = user(1L, "token-p1");
        User defenderUser = user(2L, "token-p2");
        Player attacker = player(1L, 50, WizardClass.BALANCEDWIZARD);
        Player defender = player(2L, 10, WizardClass.BALANCEDWIZARD);
        wireUsersAndPlayers(attackerUser, defenderUser, attacker, defender);

        battleService.resolveAttack("ABC123", "token-p1", "FIREBALL");

        assertEquals(GameStatus.BATTLE, session.getGameStatus());
        assertNull(session.getWinnerId());
        assertEquals(2L, session.getActivePlayerId());
    }

    @Test
    void resolveAttack_attackWizardFireInHotClear_appliesWizardAndWeatherMultipliers() {
        // FIREBALL(30) * ATTACKWIZARD(1.5) * HOT(1.2 on FIRE) * CLEAR(1.2 on FIRE) = 64 (int cast of 64.8)
        primeBattle(TemperatureCategory.HOT, RainCategory.CLEAR, 1);
        User attackerUser = user(1L, "token-p1");
        User defenderUser = user(2L, "token-p2");
        Player attacker = player(1L, 100, WizardClass.ATTACKWIZARD);
        Player defender = player(2L, 100, WizardClass.TANKWIZARD);
        wireUsersAndPlayers(attackerUser, defenderUser, attacker, defender);

        battleService.resolveAttack("ABC123", "token-p1", "FIREBALL");

        assertEquals(36, defender.getHp());
    }

    @Test
    void resolveAttack_fireInRainyCold_dampenedByWeather() {
        // FIREBALL(30) * BALANCED(1.0) * COLD(0.8 on FIRE) * RAINING(0.8 on FIRE) = 19 (int cast of 19.2)
        primeBattle(TemperatureCategory.COLD, RainCategory.RAINING, 1);
        User attackerUser = user(1L, "token-p1");
        User defenderUser = user(2L, "token-p2");
        Player attacker = player(1L, 100, WizardClass.BALANCEDWIZARD);
        Player defender = player(2L, 100, WizardClass.BALANCEDWIZARD);
        wireUsersAndPlayers(attackerUser, defenderUser, attacker, defender);

        battleService.resolveAttack("ABC123", "token-p1", "FIREBALL");

        assertEquals(81, defender.getHp());
    }

    @Test
    void resolveAttack_lightningInRain_boostedByWeather() {
        // LIGHTNING(30) * BALANCED(1.0) * NEUTRAL(1.0 on LIGHTNING) * RAINING(1.4 on LIGHTNING) = 42
        primeBattle(TemperatureCategory.NEUTRAL, RainCategory.RAINING, 1);
        User attackerUser = user(1L, "token-p1");
        User defenderUser = user(2L, "token-p2");
        Player attacker = player(1L, 100, WizardClass.BALANCEDWIZARD);
        Player defender = player(2L, 100, WizardClass.BALANCEDWIZARD);
        wireUsersAndPlayers(attackerUser, defenderUser, attacker, defender);

        battleService.resolveAttack("ABC123", "token-p1", "LIGHTNING");

        assertEquals(58, defender.getHp());
    }

    @Test
    void buildBattleState_setsMaxHpCorrectly() {
        Player p1 = new Player();
        p1.setUserId(1L);
        p1.setHp(80);
        p1.setMaxHp(100);

        Player p2 = new Player();
        p2.setUserId(2L);
        p2.setHp(40);
        p2.setMaxHp(150);

        given(playerRepository.findByUserIdAndGameSessionId(1L, 1L)).willReturn(p1);
        given(playerRepository.findByUserIdAndGameSessionId(2L, 1L)).willReturn(p2);

        given(userRepository.findById(1L)).willReturn(Optional.of(user(1L, "t1")));
        given(userRepository.findById(2L)).willReturn(Optional.of(user(2L, "t2")));

        BattleStateDTO dto = battleService.buildBattleState(session, 0, null);

        assertEquals(100, dto.getPlayer1MaxHp());
        assertEquals(150, dto.getPlayer2MaxHp());
    }

    // ---- helpers ----

    private void primeBattle(TemperatureCategory temp, RainCategory rain, int turnsAfterSave) {
        session.setGameStatus(GameStatus.BATTLE);
        session.setTemperature(temp);
        session.setRain(rain);
        session.setActivePlayerId(1L);
        session.setWinnerId(null);
        given(battleRepository.countTurnsByGameId(1L)).willReturn(turnsAfterSave);
    }

    private void wireUsersAndPlayers(User attackerUser, User defenderUser, Player attacker, Player defender) {
        given(authenticationService.authenticateByToken(attackerUser.getToken())).willReturn(attackerUser);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);
        given(playerRepository.findByUserIdAndGameSessionId(attackerUser.getId(), 1L)).willReturn(attacker);
        given(playerRepository.findByUserIdAndGameSessionId(defenderUser.getId(), 1L)).willReturn(defender);
        given(userRepository.findById(attackerUser.getId())).willReturn(Optional.of(attackerUser));
        given(userRepository.findById(defenderUser.getId())).willReturn(Optional.of(defenderUser));
    }

    private User user(Long id, String token) {
        User u = new User();
        u.setId(id);
        u.setToken(token);
        u.setUsername("user" + id);
        return u;
    }

    private Player player(Long userId, int hp, WizardClass cls) {
        Player p = new Player();
        p.setUserId(userId);
        p.setGameSessionId(1L);
        p.setHp(hp);
        p.setMaxHp(hp);
        p.setWizardClass(cls);
        // startTimer reads these when the battle continues
        p.setAttack1("FIREBALL");
        p.setAttack2("PUNCH");
        p.setAttack3("TORNADO");
        return p;
    }
}