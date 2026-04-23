package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Battle;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.BattleRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleResultGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import ch.uzh.ifi.hase.soprafs26.constant.Element;
import ch.uzh.ifi.hase.soprafs26.constant.WeatherModifier;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.ScheduledFuture;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import java.util.concurrent.Executors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Core battle orchestrator. Resolves incoming attacks, computes damage
 * (attack base × wizard class multiplier × weather modifier), logs each
 * turn, determines the winner, and broadcasts the updated BattleStateDTO
 * to all subscribers of /topic/game/{gameCode}. Also owns a per-game
 * auto-attack timer that fires if the active player doesn't act in time.
 */
@Service
@Transactional
public class BattleService {
    private final Logger log = LoggerFactory.getLogger(BattleService.class);
    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;
    private final AuthenticationService authenticationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final BattleRepository battleRepository;
    private final Map<String, ScheduledFuture<?>> activeTimers = new ConcurrentHashMap<>();
    private final TaskScheduler taskScheduler = new ConcurrentTaskScheduler(Executors.newScheduledThreadPool(2));

    public BattleService(GameSessionRepository gameSessionRepository,
                         PlayerRepository playerRepository,
                         AuthenticationService authenticationService,
                         SimpMessagingTemplate messagingTemplate,
                         UserRepository userRepository,
                         BattleRepository battleRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.playerRepository = playerRepository;
        this.authenticationService = authenticationService;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.battleRepository = battleRepository;
    }

    public void resolveAttack(String gameCode, String token, String attackName){
        User user = authenticationService.authenticateByToken(token);
        GameSession session = gameSessionRepository.findByGameCode(gameCode);
        stopTimer(gameCode);

        if (session == null || session.getGameStatus() != GameStatus.BATTLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game not in battle phase.");
        }

        if (!user.getId().equals(session.getActivePlayerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "It's not your turn.");
        }

        Player attacker = playerRepository.findByUserIdAndGameSessionId(session.getActivePlayerId(), session.getId());

        Long defenderId;
        if (session.getPlayer1Id().equals(session.getActivePlayerId())) {
            defenderId = session.getPlayer2Id();
        } else {
            defenderId = session.getPlayer1Id();
        }

        Player defender = playerRepository.findByUserIdAndGameSessionId(defenderId, session.getId());

        Attack attack = Attack.valueOf(attackName);
        int damage = calculateDamage(attack, attacker, session);

        // logging the battle for history and game stats
        Battle battleLog = new Battle();
        battleLog.setGameId(session.getId());
        battleLog.setPlayer1Id(session.getPlayer1Id());
        battleLog.setPlayer2Id(session.getPlayer2Id());
        battleLog.setActivePlayerId(user.getId());
        battleLog.setCurrentAction(attack);
        battleLog.setDamageDealt(damage);
        battleLog.setTimeStamp(LocalDateTime.now());
        battleRepository.save(battleLog);

        defender.setHp(defender.getHp() - damage);
        playerRepository.save(defender);

        // a "round" = both players have attacked, so the battle can only end on even turn counts;
        // this guarantees the second player always gets a response swing before a loss is declared.
        boolean isEvenTurn = battleRepository.countTurnsByGameId(session.getId()) % 2 == 0;
        boolean battleEndedAfterRound = isEvenTurn && (attacker.getHp() <= 0 || defender.getHp() <= 0);
        if (battleEndedAfterRound) {
            if (attacker.getHp() > defender.getHp()) {
                session.setWinnerId(attacker.getUserId()); // Attacker wins
            } else if (defender.getHp() > attacker.getHp()) {
                session.setWinnerId(defender.getUserId()); // Defender wins
            } else {
                session.setWinnerId(null); // Draw
            }
            // Clear current game session for both players
            session.setGameStatus(GameStatus.FINISHED);
            User attackerUser = userRepository.findById(attacker.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attacker user not found."));
            attackerUser.setCurrentGameSessionId(null);
            userRepository.save(attackerUser);
            User defenderUser = userRepository.findById(defender.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Defender user not found."));
            defenderUser.setCurrentGameSessionId(null);
            userRepository.save(defenderUser);
        } else {
            session.setActivePlayerId(defenderId);
            
        }

        gameSessionRepository.save(session);
        BattleStateDTO state = buildBattleState(session, damage, attackName);
        startTimer(gameCode, session, state);
        broadcastAfterCommit(gameCode, state);
    }

    // Defers the WebSocket send until after the DB transaction commits, so clients
    // re-reading state on the broadcast never observe a stale/uncommitted view.
    private void broadcastAfterCommit(String gameCode, BattleStateDTO state) {
        String destination = "/topic/game/" + gameCode;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    timedSend(destination, state);
                }
            });
        } else {
            timedSend(destination, state);
        }
    }

    private void timedSend(String destination, BattleStateDTO state) {
        long start = System.nanoTime();
        messagingTemplate.convertAndSend(destination, state);
        log.info("battle broadcast {} took {}ms", destination, (System.nanoTime() - start) / 1_000_000);
    }

    // Damage = attack.baseDamage × wizard class multiplier × weather modifier.
    // The weather modifier boosts or dampens an element based on the current
    // temperature & rain (e.g. fire attacks do more in HOT/CLEAR weather).
    private int calculateDamage(Attack attack, Player attacker, GameSession session) {
        Element element = attack.getElement();
        TemperatureCategory temperature = session.getTemperature();
        RainCategory rain = session.getRain();

        Map<Object, Double> elementMatrix = WeatherModifier.ELEMENT_MODIFIERS.get(element);
        double tempFactor = elementMatrix.getOrDefault(temperature, 1.0);
        double rainFactor = elementMatrix.getOrDefault(rain, 1.0);
        double weatherModifier = tempFactor * rainFactor;

        return (int)(attack.getBaseDamage()
                   * attacker.getWizardClass().getAttackMultiplier()
                   * weatherModifier);
    }

    public BattleStateDTO getBattleState(String gameCode) {
        GameSession session = gameSessionRepository.findByGameCode(gameCode);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found.");
        }

        if (session.getPlayer2Id() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Battle not ready yet.");
        }

        return buildBattleState(session, 0, null);
    }


    public BattleStateDTO buildBattleState(GameSession session, int damage, String attackName) {
        Player player1 = playerRepository.findByUserIdAndGameSessionId(session.getPlayer1Id(), session.getId());
        Player player2 = playerRepository.findByUserIdAndGameSessionId(session.getPlayer2Id(), session.getId());

        if (player1 == null || player2 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Players not found.");
        }

        User user1 = userRepository.findById(session.getPlayer1Id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User1 not found"));
        User user2 = userRepository.findById(session.getPlayer2Id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User2 not found"));

        BattleStateDTO dto = new BattleStateDTO();
        dto.setActivePlayerId(session.getActivePlayerId());
        dto.setPlayer1Hp(player1.getHp());
        dto.setPlayer2Hp(player2.getHp());
        dto.setDamageDealt(damage);
        dto.setAttackUsed(attackName);
        dto.setGameStatus(session.getGameStatus());
        dto.setWinnerId(session.getWinnerId());
        dto.setTimeStamp(LocalDateTime.now());

        dto.setPlayer1UserId(session.getPlayer1Id());
        dto.setPlayer2UserId(session.getPlayer2Id());
        dto.setPlayer1Username(user1.getUsername());
        dto.setPlayer2Username(user2.getUsername());
        dto.setPlayer1WizardClass(player1.getWizardClass() != null ? player1.getWizardClass().name() : "Unknown");
        dto.setPlayer2WizardClass(player2.getWizardClass() != null ? player2.getWizardClass().name() : "Unknown");

        dto.setLocation(session.getArenaLocation() != null ? session.getArenaLocation().name() : "Unknown");
        dto.setRain(session.getRain());
        dto.setTemperature(session.getTemperature());
        return dto;
    }

    public BattleResultGetDTO getBattleResult(String gameCode){
        GameSession session = gameSessionRepository.findByGameCode(gameCode);
        if (session == null || session.getGameStatus() != GameStatus.FINISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Battle not finished or not found.");
        }

        WeatherGetDTO weatherDTO = new WeatherGetDTO();
        weatherDTO.setRainCategory(session.getRain());
        weatherDTO.setTemperatureCategory(session.getTemperature());

        BattleResultGetDTO result = new BattleResultGetDTO();
	
        // draw results in null winner and loser
        Long winnerUserId = session.getWinnerId();
        result.setWinnerUserId(winnerUserId);
        Long loserUserId = null;
        if (winnerUserId != null) {
            loserUserId = winnerUserId.equals(session.getPlayer1Id())
                ? session.getPlayer2Id()
                : session.getPlayer1Id();
        }
        result.setLoserUserId(loserUserId);

        Integer totalDamage = battleRepository.sumDamageByGameId(session.getId());
        result.setTotalDamageDealt(totalDamage != null ? totalDamage : 0);

        int turnsPlayed = battleRepository.countTurnsByGameId(session.getId());
        result.setTurnsPlayed(turnsPlayed);
        
        result.setWeather(weatherDTO);

        return result;
    } 

    private void stopTimer(String gameCode) {
        ScheduledFuture<?> timer = activeTimers.remove(gameCode);
        if (timer != null) {
            timer.cancel(false); 
        }
    }
    
    // If the active player doesn't attack within 30s, a random one of their
    // three selected attacks fires automatically on their behalf so the
    // battle can't stall indefinitely. Any new startTimer call cancels the
    // previous one for the same gameCode.
    public void startTimer(String gameCode, GameSession session) {
        stopTimer(gameCode);
        Player attacker = playerRepository.findByUserIdAndGameSessionId(
            session.getActivePlayerId(),
            session.getId()
        );
        
        LocalDateTime startTime = dto.getTimeStamp();
        Instant executionTime = startTime.plusSeconds(30).atZone(ZoneId.systemDefault()).toInstant();
        
        ScheduledFuture<?> task = taskScheduler.schedule(() -> {
            List<String> playerAttacks = new ArrayList<>();
            playerAttacks.add(attacker.getAttack1());
            playerAttacks.add(attacker.getAttack2());
            playerAttacks.add(attacker.getAttack3());

            String attackName = playerAttacks.get((int) (Math.random() * 3));

            // use the active player's own token so resolveAttack's auth check passes
            User activeUser = userRepository.findById(session.getActivePlayerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active user not found"));
            String token = activeUser.getToken();

            resolveAttack(gameCode, token, attackName);
        }, Instant.now().plusSeconds(30));

        activeTimers.put(gameCode, task);
    }

}