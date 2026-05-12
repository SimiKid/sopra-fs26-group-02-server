package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Coordinates rematch requests. A rematch is only created once BOTH
 * players have opted in; the first requester's vote is stored on the
 * old session and a new game is generated when the second vote arrives.
 */
@Service
@Transactional
public class RematchService {

    private final GameSessionRepository gameSessionRepository;
    private final AuthenticationService authenticationService;
    private final GameSessionService gameSessionService;
    private final UserRepository userRepository;

    public RematchService(GameSessionRepository gameSessionRepository,
                          AuthenticationService authenticationService,
                          GameSessionService gameSessionService,
                            UserRepository userRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.authenticationService = authenticationService;
        this.gameSessionService = gameSessionService;
        this.userRepository = userRepository;
    }

    public void requestRematch(String gameCode, String token) {
        User user = authenticationService.authenticateByToken(token);
        Long userId = user.getId();

        GameSession oldSession = Optional.ofNullable(gameSessionRepository.findByGameCode(gameCode))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found."));

        if (oldSession.getGameStatus() != GameStatus.FINISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not finished yet.");
        }

        // rematch already created by the other player's request -> nothing to do
        if (oldSession.getRematchGameCode() != null) {
            return;
        }

        registerRematchVote(userId, oldSession);
        gameSessionRepository.save(oldSession);

        // re-read to pick up the other player's vote if they saved concurrently
        GameSession freshSession = Optional.ofNullable(gameSessionRepository.findByGameCode(gameCode))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found."));

        // both players have opted in -> spin up the rematch game
        if (freshSession.getPlayer1WantsRematch() && freshSession.getPlayer2WantsRematch()) {
            String newCode = createRematchGame(freshSession.getPlayer1Id(), freshSession.getPlayer2Id(),  freshSession.getId());
            freshSession.setRematchGameCode(newCode);
            freshSession.setPlayer1WantsRematch(false);
            freshSession.setPlayer2WantsRematch(false);
            gameSessionRepository.save(freshSession);
        }
    }

    private void registerRematchVote(Long userId, GameSession oldSession) {
        boolean isPlayer1 = userId.equals(oldSession.getPlayer1Id());
        boolean isPlayer2 = userId.equals(oldSession.getPlayer2Id());
        if (!isPlayer1 && !isPlayer2) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant of this game.");
        }

        if (isPlayer1) {
            oldSession.setPlayer1WantsRematch(true);
        } else {
            oldSession.setPlayer2WantsRematch(true);
        }
    }

    private String createRematchGame(Long player1Id, Long player2Id, Long oldSession) {
        User user1 = userRepository.findById(player1Id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User1 not found."));
        User user2 = userRepository.findById(player2Id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User2 not found."));

        boolean p1InOtherGame = user1.getCurrentGameSessionId() != null
        && !user1.getCurrentGameSessionId().equals(oldSession);
        boolean p2InOtherGame = user2.getCurrentGameSessionId() != null
            && !user2.getCurrentGameSessionId().equals(oldSession);

        if (p1InOtherGame || p2InOtherGame) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "One or both players have already joined another game.");
        }
        
        gameSessionService.nullifyGameSessionId(user1.getId());
        gameSessionService.nullifyGameSessionId(user2.getId());

        GameSession newSession = new GameSession();
        newSession.setPlayer1Id(player1Id);
        GameSession created = gameSessionService.createGameSession(newSession);
        gameSessionService.joinGameSession(created.getGameCode(), player2Id);

        return created.getGameCode();
    }
}