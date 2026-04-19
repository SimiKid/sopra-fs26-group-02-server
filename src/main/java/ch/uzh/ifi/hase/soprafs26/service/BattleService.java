package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
@Service
public class BattleService {
    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;
    private final AuthenticationService authenticationService;
    private final SimpMessagingTemplate messagingTemplate;

    public BattleService(GameSessionRepository gameSessionRepository,
                         PlayerRepository playerRepository,
                         AuthenticationService authenticationService,
                         SimpMessagingTemplate messagingTemplate) {
        this.gameSessionRepository = gameSessionRepository;
        this.playerRepository = playerRepository;
        this.authenticationService = authenticationService;
        this.messagingTemplate = messagingTemplate;
    }

    public void resolveAttack(String gameCode, String token, String attackName){
        User user = authenticationService.authenticateByToken(token);
        GameSession session = gameSessionRepository.findByGameCode(gameCode);

        if (!user.getId().equals(session.getActivePlayerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "It's not your turn.");
        }

        Player attacker = playerRepository.findByUserId(session.getActivePlayerId());

        Long defenderId;
        if (session.getPlayer1Id().equals(session.getActivePlayerId())) {
            defenderId = session.getPlayer2Id();
        } else {
            defenderId = session.getPlayer1Id();
        }

        Player defender = playerRepository.findByUserId(defenderId);

        Attack attack = Attack.valueOf(attackName);
        int damage = calculateDamage(attack, attacker, 1.0); // 1.0 = weather placeholder for #54

        defender.setHp(defender.getHp() - damage);
        playerRepository.save(defender);

        if (defender.getHp() <= 0) {
            session.setGameStatus(GameStatus.FINISHED);
            session.setWinnerId(user.getId());
        } else {
            session.setActivePlayerId(defenderId);
        }

        gameSessionRepository.save(session);

        BattleStateDTO state = buildBattleState(session, attacker, defender, damage, attackName);
        messagingTemplate.convertAndSend("/topic/game/" + gameCode, state);
    }

    private int calculateDamage(Attack attack, Player attacker, double weatherMultiplier) {
        return (int)(attack.getBaseDamage()
                   * attacker.getWizardClass().getAttackMultiplier()
                   * weatherMultiplier);
    }

    private BattleStateDTO buildBattleState(GameSession session, Player attacker, Player defender, int damage, String attackName) {
        BattleStateDTO dto = new BattleStateDTO();
        dto.setActivePlayerId(session.getActivePlayerId());
        dto.setPlayer1Hp(playerRepository.findByUserId(session.getPlayer1Id()).getHp());
        dto.setPlayer2Hp(playerRepository.findByUserId(session.getPlayer2Id()).getHp());
        dto.setDamageDealt(damage);
        dto.setAttackUsed(attackName);
        dto.setGameStatus(session.getGameStatus());
        dto.setWinnerId(session.getWinnerId());
        return dto;
    }

    public BattleStateDTO getBattleState(String gameCode) {
        GameSession session = gameSessionRepository.findByGameCode(gameCode);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found.");
        }

        if (session.getPlayer2Id() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Battle not ready yet.");
        }

        Player player1 = playerRepository.findByUserId(session.getPlayer1Id());
        Player player2 = playerRepository.findByUserId(session.getPlayer2Id());

        if (player1 == null || player2 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Players not found.");
        }

        BattleStateDTO dto = new BattleStateDTO();
        dto.setActivePlayerId(session.getActivePlayerId());
        dto.setPlayer1Hp(player1.getHp());
        dto.setPlayer2Hp(player2.getHp());
        dto.setDamageDealt(0);
        dto.setAttackUsed(null);
        dto.setGameStatus(session.getGameStatus());
        dto.setWinnerId(session.getWinnerId());

        return dto;
    }
}