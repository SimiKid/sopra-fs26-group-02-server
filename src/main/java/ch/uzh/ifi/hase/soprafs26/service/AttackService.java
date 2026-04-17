package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;



@Service
@Transactional
public class AttackService {
    private final Logger log = LoggerFactory.getLogger(AttackService.class);
    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final SimpMessagingTemplate messagingTemplate;


    public AttackService(PlayerRepository playerRepository, GameSessionRepository gameSessionRepository, UserRepository userRepository, AuthenticationService authenticationService, SimpMessagingTemplate messagingTemplate) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository=gameSessionRepository;
        this.userRepository=userRepository;
        this.authenticationService = authenticationService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<AttackGetDTO> getAllAttacks() {
        List<AttackGetDTO> attackDTOs = new ArrayList<>();
        for (Attack attack : Attack.values()) {
            AttackGetDTO dto = new AttackGetDTO();
            dto.setId(attack.getId());
            dto.setName(attack.getDisplayName());
            dto.setBaseDamage(attack.getBaseDamage());
            dto.setElement(attack.getElement());
            dto.setDescription(attack.getDescription());
            attackDTOs.add(dto);
            }
            return attackDTOs;
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

        //check if the currentSessionID of the user found above is the same as the one in the game session found above
        if (user.getCurrentGameSessionId() == null || !user.getCurrentGameSessionId().equals(session.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not assigned to this game session in their profile.");
        }

        //check if each attacks is part auf our constant Attack.java
        for (String attackId : attacks) {
            try {
                Attack.valueOf(attackId);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown attack name: " + attackId);
            }
        }
        // Find the player for this userId 
        Player player = playerRepository.findByUserId(user.getId());
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found for the given user in this game.");
        }            
        player.setAttack1(attacks.get(0));
        player.setAttack2(attacks.get(1));
        player.setAttack3(attacks.get(2));

        player.setReady(true);
        playerRepository.save(player);

        //set gamesession status to battle when both players are ready
        Player player1=playerRepository.findByUserId(session.getPlayer1Id());
        if (player1 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player1 not found");
        }
        Player player2=playerRepository.findByUserId(session.getPlayer2Id());
        if (player2 == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player2 not found");
        }
        log.info("Ready check — p1(userId={}) ready={}, p2(userId={}) ready={}",
            session.getPlayer1Id(), player1.isReady(), session.getPlayer2Id(), player2.isReady());
        if (player1.isReady() && player2.isReady()) {
            session.setGameStatus(GameStatus.BATTLE);
            session.setActivePlayerId(
                Math.random() < 0.5 ? session.getPlayer1Id() : session.getPlayer2Id()
            );
            gameSessionRepository.save(session);

            BattleStateDTO state = new BattleStateDTO();
            state.setActivePlayerId(session.getActivePlayerId());
            state.setPlayer1Hp(player1.getHp());
            state.setPlayer2Hp(player2.getHp());
            state.setDamageDealt(0);
            state.setAttackUsed(null);
            state.setGameStatus(GameStatus.BATTLE);
            state.setWinnerId(null);
            messagingTemplate.convertAndSend("/topic/game/" + gameCode, state);
        }

        return playerRepository.save(player);
    }
}






