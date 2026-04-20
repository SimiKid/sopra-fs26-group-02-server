package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
import ch.uzh.ifi.hase.soprafs26.service.BattleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttackService {
    private final Logger log = LoggerFactory.getLogger(AttackService.class);

    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BattleService battleService;

    public AttackService(PlayerRepository playerRepository, GameSessionRepository gameSessionRepository, UserRepository userRepository, AuthenticationService authenticationService, SimpMessagingTemplate messagingTemplate, BattleService battleService) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository=gameSessionRepository;
        this.userRepository=userRepository;
        this.authenticationService = authenticationService;
        this.messagingTemplate = messagingTemplate;
        this.battleService = battleService;
    }

    public List<Attack> getAllAttacks() {
        return List.of(Attack.values());
    }

    public Player getAttacks(String gameCode, String token) {
        User user = authenticationService.authenticateByToken(token);

        GameSession session = Optional.ofNullable(gameSessionRepository.findByGameCode(gameCode))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found."));

        if (!user.getId().equals(session.getPlayer1Id()) && !user.getId().equals(session.getPlayer2Id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of this game.");
        }

        Player player = playerRepository.findByUserIdAndGameSessionId(user.getId(), session.getId());
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found for the given user in this game.");
        }
        return player;
    }
    
  
    public Player setAttacks(String gameCode, List<String> attacks, String token) {
        //more checks that could be added: check if wizard is already selected, check if two players are in the gamesession
        
        //authenticate the user
        User user = authenticationService.authenticateByToken(token);
        
        //check if exactly three attacks are given
        if (attacks == null || attacks.size() != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exactly 3 attacks must be selected.");
        }
        //check if the gameCode given exists as GameSession
        GameSession session = gameSessionRepository.findByGameCode(gameCode);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found.");
        }
        //check if the above found session has the userId saved either in player1 or player2 (player is part of the game)
        if (!user.getId().equals(session.getPlayer1Id()) && !user.getId().equals(session.getPlayer2Id())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not part of this game.");
        }

        if (user.getCurrentGameSessionId() == null || !user.getCurrentGameSessionId().equals(session.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not assigned to this game session in their profile.");
        }

        for (String attackId : attacks) {
            try {
                Attack.valueOf(attackId);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown attack name: " + attackId);
            }
        }
        // Find the player for this userId
        Player player = playerRepository.findByUserIdAndGameSessionId(user.getId(), session.getId());
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found for the given user in this game.");
        }
        player.setAttack1(attacks.get(0));
        player.setAttack2(attacks.get(1));
        player.setAttack3(attacks.get(2));
        player.setReady(true);
        playerRepository.save(player);

        //set gamesession status to battle when both players are ready
        Player savedPlayer = playerRepository.save(player);
        Player player1 = playerRepository.findByUserIdAndGameSessionId(session.getPlayer1Id(), session.getId());
        if (player1 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player1 not found");
        }

        if (session.getPlayer2Id() == null) {
            return savedPlayer;
        }

        Player player2 = playerRepository.findByUserIdAndGameSessionId(session.getPlayer2Id(), session.getId());
        if (player2 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player2 not found");
        }
        if (player1.isReady() && player2.isReady()) {
            session.setGameStatus(GameStatus.BATTLE);
            session.setActivePlayerId(
                Math.random() < 0.5 ? session.getPlayer1Id() : session.getPlayer2Id()
            );
        
            battleService.startTimer(session.getGameCode());
            gameSessionRepository.save(session);

            User user1 = userRepository.findById(session.getPlayer1Id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User1 not found"));
            User user2 = userRepository.findById(session.getPlayer2Id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User2 not found"));

            BattleStateDTO initialState = new BattleStateDTO();
            initialState.setActivePlayerId(session.getActivePlayerId());
            initialState.setPlayer1Hp(player1.getHp());
            initialState.setPlayer2Hp(player2.getHp());
            initialState.setDamageDealt(0);
            initialState.setAttackUsed(null);
            initialState.setGameStatus(session.getGameStatus());
            initialState.setWinnerId(session.getWinnerId());

            initialState.setPlayer1UserId(session.getPlayer1Id());
            initialState.setPlayer2UserId(session.getPlayer2Id());
            initialState.setPlayer1Username(user1.getUsername());
            initialState.setPlayer2Username(user2.getUsername());
            initialState.setPlayer1WizardClass(player1.getWizardClass() != null ? player1.getWizardClass().name() : "Unknown");
            initialState.setPlayer2WizardClass(player2.getWizardClass() != null ? player2.getWizardClass().name() : "Unknown");

            initialState.setLocation(session.getArenaLocation() != null ? session.getArenaLocation().name() : "Unknown");
            initialState.setRain(session.getRain());
            initialState.setTemperature(session.getTemperature());

            messagingTemplate.convertAndSend("/topic/game/" + gameCode, initialState);
        }
        return savedPlayer;
    }
}