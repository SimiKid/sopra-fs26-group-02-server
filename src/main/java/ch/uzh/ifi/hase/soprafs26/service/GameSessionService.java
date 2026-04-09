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
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


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

	public GameSessionService(@Qualifier("gameSessionRepository") GameSessionRepository gameSessionRepository) {
		this.gameSessionRepository = gameSessionRepository;
	}

	public List<GameSession> getGameSessions() {
    return this.gameSessionRepository.findAll();
}

	// The for-loop tries to create a unique game code. If it fails after MAX_ATTEMPTS, it will throw an exception.
	public GameSession createGameSession(GameSession newGameSession) {
		newGameSession.setGameStatus(GameStatus.WAITING);
		newGameSession.setCreatedAt(LocalDateTime.now());
		newGameSession.setActivePlayerId(newGameSession.getPlayer1Id());

		for (int i = 0; i < 5; i++) {
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
		String gameCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return gameCode;
	}

	public boolean deleteByGameCode(String gameCode) {
		GameSession gameSession = gameSessionRepository.findByGameCode(gameCode);
		if (gameSession == null) {
			return false;
		}
		gameSessionRepository.delete(gameSession);
		return true;
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
}
