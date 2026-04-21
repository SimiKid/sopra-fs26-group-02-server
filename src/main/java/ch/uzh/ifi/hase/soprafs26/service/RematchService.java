package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;

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

    public RematchService(GameSessionRepository gameSessionRepository,
                          AuthenticationService authenticationService,
                          GameSessionService gameSessionService) {
        this.gameSessionRepository = gameSessionRepository;
        this.authenticationService = authenticationService;
        this.gameSessionService = gameSessionService;
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

        if (oldSession.getPlayer1WantsRematch() && oldSession.getPlayer2WantsRematch()) {
            String newCode = createRematchGame(oldSession);
            oldSession.setRematchGameCode(newCode);
            oldSession.setPlayer1WantsRematch(false);
            oldSession.setPlayer2WantsRematch(false);
            gameSessionRepository.save(oldSession);
        }
    }

    private String createRematchGame(GameSession oldSession) {
        Long player1Id = oldSession.getPlayer1Id();
        Long player2Id = oldSession.getPlayer2Id();

        gameSessionService.clearPlayerCurrentSessions(player1Id, player2Id);

        GameSession newSession = new GameSession();
        newSession.setPlayer1Id(player1Id);
        GameSession created = gameSessionService.createGameSession(newSession);
        gameSessionService.joinGameSession(created.getGameCode(), player2Id);

        return created.getGameCode();
    }
}