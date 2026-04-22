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
import java.util.Optional;
import java.util.Random;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.constant.Location;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LocationGetDTO;

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
	private final PlayerRepository playerRepository;
	private final UserRepository userRepository;
	private final WeatherService weatherService;

	public GameSessionService(@Qualifier("gameSessionRepository") GameSessionRepository gameSessionRepository, @Qualifier("playerRepository") PlayerRepository playerRepository, UserRepository userRepository, WeatherService weatherService) {
		this.gameSessionRepository = gameSessionRepository;
		this.playerRepository = playerRepository;
		this.userRepository = userRepository;
		this.weatherService = weatherService;
	}

	private static final int MAX_ATTEMPTS = 5;

	private static final Random RANDOM = new Random();
	private static final int LOCSIZE = Location.values().length - 1; // exclude fallback location

	public List<GameSession> getGameSessions() {
    return this.gameSessionRepository.findAll();
}

	// The for-loop tries to create a unique game code. If it fails after MAX_ATTEMPTS, it will throw an exception.
	public GameSession createGameSession(GameSession newGameSession) {
		newGameSession.setGameStatus(GameStatus.WAITING);
		newGameSession.setCreatedAt(LocalDateTime.now());
		newGameSession.setActivePlayerId(newGameSession.getPlayer1Id());


		// random location from enum
		newGameSession.setArenaLocation(Location.values()[RANDOM.nextInt(LOCSIZE)]);
		// get & set weather for location
		WeatherGetDTO weather = weatherService.getWeatherForLocation(newGameSession, newGameSession.getArenaLocation());
		newGameSession.setRain((RainCategory) weather.getRainCategory());
		newGameSession.setTemperature((TemperatureCategory) weather.getTemperatureCategory());

		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			String code = createGameCode();
			if (gameSessionRepository.existsByGameCode(code)) {
				continue;
			}

			newGameSession.setGameCode(code);
			try {
				GameSession saved = gameSessionRepository.save(newGameSession);
				gameSessionRepository.flush(); // forces unique-constraint check

				// create Player 1
				Player player1 = new Player();
				player1.setUserId(saved.getPlayer1Id());
				player1.setGameSessionId(saved.getId());
				player1.setReady(false);
				playerRepository.save(player1);

				User user = userRepository.findById(saved.getPlayer1Id())
                	.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
				if (user.getCurrentGameSessionId() != null) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "User already is in a game session.");
				}
            	user.setCurrentGameSessionId(saved.getId()); 
                userRepository.save(user); 

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
		return Optional.ofNullable(gameSessionRepository.findByGameCode(gameCode))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or expired."));
	}

    // This method creates a random 6-character game code consisting of uppercase letters and digits.
	private String createGameCode() {
		return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		}

	public GameSession joinGameSession(String gameCode, Long player2Id) {
		GameSession gameSession = Optional.ofNullable(gameSessionRepository.findByGameCode(gameCode))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found or expired."));

		if (gameSession.getGameStatus() != GameStatus.WAITING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is not accepting players.");
		}

		if (gameSession.getPlayer2Id() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Game is already full.");
		}

		if (gameSession.getPlayer1Id().equals(player2Id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "You cannot join your own game.");
		}

		gameSession.setPlayer2Id(player2Id);
		gameSession.setGameStatus(GameStatus.CONFIGURING);

		gameSession = gameSessionRepository.save(gameSession);
		gameSessionRepository.flush();

		// create Player 2
		Player player2 = new Player();
		player2.setUserId(player2Id);
		player2.setGameSessionId(gameSession.getId());
		player2.setReady(false);
		playerRepository.save(player2);

		User user = userRepository.findById(player2Id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getCurrentGameSessionId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already is in another game session.");
        }
        user.setCurrentGameSessionId(gameSession.getId()); 
        userRepository.save(user); 

		log.info("Player {} joined game session {}", player2Id, gameCode);
		return gameSession;
	}

	public void deleteByGameCode(String gameCode) {
		GameSession gameSession = Optional.ofNullable(gameSessionRepository.findByGameCode(gameCode))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found"));
		User user=userRepository.findById(gameSession.getPlayer1Id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setCurrentGameSessionId(null);
        userRepository.save(user);
		if (gameSession.getPlayer2Id() != null) {
        	user=userRepository.findById(gameSession.getPlayer2Id())
            	.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
			user.setCurrentGameSessionId(null);
			userRepository.save(user);
		}
		gameSessionRepository.delete(gameSession);
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

	public Player saveWizardClass(String gameCode, String token, String wizardClassName) {
		GameSession gameSession = getByGameCode(gameCode);

		if (gameSession.getGameStatus() != GameStatus.CONFIGURING) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Game is not in configuration phase.");
		}
		
		User user = userRepository.findByToken(token);
		Long userId = user.getId();
		
		if (!userId.equals(gameSession.getPlayer1Id()) && !userId.equals(gameSession.getPlayer2Id())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of this game session.");
		}
		
		Player player = playerRepository.findByUserIdAndGameSessionId(userId, gameSession.getId());
		WizardClass wc;
		try {
			wc = WizardClass.valueOf(wizardClassName);
		}
		catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid wizard class.");
		}
		player.setWizardClass(wc);
		player.setHp((int)(100 * wc.getHpMultiplier()));
		
		return playerRepository.save(player);
	}

	public WeatherGetDTO getWeatherByCode(String gameCode) {
	// fetch weather data for the game session with the given game code
	GameSession gameSession = getByGameCode(gameCode);
	
	WeatherGetDTO weatherDTO = new WeatherGetDTO();
	weatherDTO.setRainCategory(gameSession.getRain());
	weatherDTO.setTemperatureCategory(gameSession.getTemperature());

	return weatherDTO;
	}

	public LocationGetDTO getLocationDTOByCode(String gameCode) {
		GameSession gameSession = getByGameCode(gameCode);
		Location location = gameSession.getArenaLocation();
		if (location == null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Arena location is not configured for this game session.");
		}
		LocationGetDTO locationDTO = new LocationGetDTO();
		locationDTO.setLocationName(location.getDisplayName());
		return locationDTO;
	}

	public void clearPlayerCurrentSessions(Long player1Id, Long player2Id) {
		User p1 = userRepository.findById(player1Id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		p1.setCurrentGameSessionId(null);
		userRepository.save(p1);

		User p2 = userRepository.findById(player2Id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		p2.setCurrentGameSessionId(null);
		userRepository.save(p2);
	}
}
