package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.Player;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;


/**
 * Game Session Service
 * This class is the "worker" and responsible for all functionality related to
 * game sessions, such as creating new sessions, generating unique game codes,
 * and retrieving existing sessions.
 * The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class GameSessionService {

	private final Logger log = LoggerFactory.getLogger(GameSessionService.class);

	private final GameSessionRepository gameSessionRepository;
	private final PlayerRepository playerRepository;

	public GameSessionService(@Qualifier("gameSessionRepository") GameSessionRepository gameSessionRepository, @Qualifier("playerRepository") PlayerRepository playerRepository) {
		this.gameSessionRepository = gameSessionRepository;
		this.playerRepository = playerRepository;
	}

	private static final String SYMBOLS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int CODE_LENGTH = 6;
	private static final int MAX_ATTEMPTS = 20;
    private static final Random rnd = new Random();


	public List<GameSession> getGameSessions() {
    return this.gameSessionRepository.findAll();
}

	// The for-loop tries to create a unique game code. If it fails after MAX_ATTEMPTS, it will throw an exception.
	public GameSession createGameSession(GameSession newGameSession) {
		newGameSession.setGameStatus(GameStatus.WAITING);
		newGameSession.setCreatedAt(LocalDateTime.now());
		newGameSession.setActivePlayerId(newGameSession.getPlayer1Id());

		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			String code = createGameCode();
			if (gameSessionRepository.existsByGameCode(code)) {
				continue;
			}

			newGameSession.setGameCode(code);
			try {
				GameSession saved = gameSessionRepository.save(newGameSession);
				gameSessionRepository.flush(); // forces unique-constraint check
				return saved;
			} catch (DataIntegrityViolationException e) {
				log.warn("Generated game code already exists. Retrying");
			}
		}
		throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"Could not generate unique game code. Please try again."
		);
	}

	public GameSession getByGameCode(String gameCode) {
		GameSession gameSession = gameSessionRepository.findByGameCode(gameCode);
		if (gameSession == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or expired.");
		}
		return gameSession;
	}

    // This method creates a random 6-character game code consisting of uppercase letters and digits.
	private String createGameCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(SYMBOLS.charAt(rnd.nextInt(SYMBOLS.length())));
        }
        return sb.toString();
    }

	// This method schedules a cleanup of game sessions that are still waiting for a second player after 10 minutes.
	// It finds all game sessions where player2Id is null and createdAt is more than 10 minutes ago, and deletes them from the repository.
	@Scheduled(fixedRate = 60000) // runs every minute
	public void cleanupExpiredGameSessions() {
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
		List<GameSession> expiredSessions = gameSessionRepository.findByPlayer2IdIsNullAndCreatedAtBefore(cutoff);
		for (GameSession session : expiredSessions) {
			log.info("Cleaning up expired game session with code {}", session.getGameCode());
			gameSessionRepository.delete(session);
		}
	}

	public Player saveWizardClass(String gameCode, Long userId, String wizardClassName) {
		GameSession gameSession = getByGameCode(gameCode);

		if (gameSession.getGameStatus() != GameStatus.CONFIGURING) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Game is not in configuration phase.");
		}
		
		if (!userId.equals(gameSession.getPlayer1Id()) && !userId.equals(gameSession.getPlayer2Id())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of this game session.");
		}
		
		Player player = playerRepository.findByUserId(userId);
		WizardClass wc = WizardClass.valueOf(wizardClassName);	
		player.setWizardClass(wc);
		player.setHp((int)(100 * wc.getHpMultiplier()));
		
		return playerRepository.save(player);
	}
}
