package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

        GameSession oldSession = gameSessionRepository.findByGameCode(gameCode);
        if (oldSession == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found.");
        }

        if (oldSession.getGameStatus() != GameStatus.FINISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not finished yet.");
        }

        if (oldSession.getRematchGameCode() != null) {
            return;
        }

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
        gameSessionRepository.save(oldSession);

        GameSession freshSession = gameSessionRepository.findByGameCode(gameCode);

        if (freshSession.getPlayer1WantsRematch() && freshSession.getPlayer2WantsRematch()) {
            String newCode = createRematchGame(freshSession);
            freshSession.setRematchGameCode(newCode);
            freshSession.setPlayer1WantsRematch(false);
            freshSession.setPlayer2WantsRematch(false);
            gameSessionRepository.save(freshSession);
        }
    }

    private String createRematchGame(GameSession oldSession) {
        Long player1Id = oldSession.getPlayer1Id();
        Long player2Id = oldSession.getPlayer2Id();

        User user1 = userRepository.findById(player1Id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User1 not found."));
        User user2 = userRepository.findById(player2Id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User2 not found."));

        boolean p1InOtherGame = user1.getCurrentGameSessionId() != null
        && !user1.getCurrentGameSessionId().equals(oldSession.getId());
        boolean p2InOtherGame = user2.getCurrentGameSessionId() != null
            && !user2.getCurrentGameSessionId().equals(oldSession.getId());

        if (p1InOtherGame || p2InOtherGame) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "One or both players have already joined another game.");
        }
        user1.setCurrentGameSessionId(null);
        user2.setCurrentGameSessionId(null);
        userRepository.save(user1);
        userRepository.save(user2);

        GameSession newSession = new GameSession();
        newSession.setPlayer1Id(player1Id);
        GameSession created = gameSessionService.createGameSession(newSession);
        gameSessionService.joinGameSession(created.getGameCode(), player2Id);

        return created.getGameCode();
    }
}