package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.BattleRepository;
import ch.uzh.ifi.hase.soprafs26.entity.Battle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TimerService {
    private final GameSessionRepository gameSessionRepository;
    private final BattleRepository battleRepository;

    public TimerService(GameSessionRepository gameSessionRepository, BattleRepository battleRepository) {
        this.gameSessionRepository = gameSessionRepository;
        this.battleRepository = battleRepository;
    }

    public LocalDateTime getEndofTurn(String gameCode) {
        GameSession gameSession = gameSessionRepository.findByGameCode(gameCode);
        if (gameSession == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found");
        }

        if (gameSession.getCurrentTurnNumber() == 0) {
            return gameSession.getStartedAt().plusSeconds(30);
        }
        
        Battle battle=battleRepository.findByGameIdAndTurnNumber(gameSession.getId(), gameSession.getCurrentTurnNumber());
        if (battle == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Battle not found");
        }
        return battle.getTimeStamp().plusSeconds(30);
    }
}
