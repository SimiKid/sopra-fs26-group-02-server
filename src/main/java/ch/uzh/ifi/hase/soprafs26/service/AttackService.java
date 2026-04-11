package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;



 

import java.util.List;
import java.util.ArrayList;



@Service
@Transactional
public class AttackService {
    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;


    public AttackService(PlayerRepository playerRepository, GameSessionRepository gameSessionRepository, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository=gameSessionRepository;
        this.userRepository=userRepository;
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

    
  
    public Player setAttacks(String gameCode, Long userId, List<String> attacks) {
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
        if (!userId.equals(session.getPlayer1Id()) && !userId.equals(session.getPlayer2Id())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not part of this game.");
        }

        //check if the userId is a registered user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        //check if the currentSession of the user found above is the same as the one in the game session found above
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
        Player player = playerRepository.findByUserId(userId);
        if (player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found for the given user in this game.");
        }            
        player.setAttack1(attacks.get(0));
        player.setAttack2(attacks.get(1));
        player.setAttack3(attacks.get(2));

        player.setReady(true);

        return playerRepository.save(player);
    }
}






