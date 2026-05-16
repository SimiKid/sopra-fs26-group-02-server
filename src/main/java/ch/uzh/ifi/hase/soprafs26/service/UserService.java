package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameResult;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameHistoryEntryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final GameSessionRepository gameSessionRepository;
	private final PlayerRepository playerRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository,
	                   @Qualifier("gameSessionRepository") GameSessionRepository gameSessionRepository,
	                   @Qualifier("playerRepository") PlayerRepository playerRepository) {
		this.userRepository = userRepository;
		this.gameSessionRepository = gameSessionRepository;
		this.playerRepository = playerRepository;
	}

	public User createUser(User newUser) {
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setCreationDate(LocalDateTime.now());
		validateUsername(newUser.getUsername());
		if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
		}
		checkIfUserExists(newUser);
		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	/**
	 * Validates the username against the registration rules: it must be
	 * present, free of spaces, and at most 20 characters long. Throws a
	 * 400 BAD_REQUEST with a rule-specific message on the first violation.
	 *
	 * @param username the raw username supplied by the client
	 * @throws org.springframework.web.server.ResponseStatusException
	 */
	private void validateUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
		}
		if (username.matches(".*\\s.*")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot contain spaces");
		}
		if (username.length() > 20) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at most 20 characters");
		}
	}

	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

		if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
		}
	}

	public List<GameHistoryEntryDTO> getGameHistory(Long userId) {
		List<GameSession> sessions = gameSessionRepository.findFinishedGamesForUser(userId);
		List<GameHistoryEntryDTO> entries = new ArrayList<>();
		for (GameSession session : sessions) {
			entries.add(buildHistoryEntry(session, userId));
		}
		return entries;
	}

	private GameHistoryEntryDTO buildHistoryEntry(GameSession session, Long userId) {
		GameHistoryEntryDTO dto = DTOMapper.INSTANCE.convertEntityToGameHistoryEntryDTO(session);
		dto.setResult(deriveResult(session, userId));

		Long opponentId = userId.equals(session.getPlayer1Id()) ? session.getPlayer2Id() : session.getPlayer1Id();
		dto.setMyWizardClass(lookupWizardClass(userId, session.getId()));
		dto.setOpponentWizardClass(opponentId == null ? null : lookupWizardClass(opponentId, session.getId()));
		return dto;
	}

	private GameResult deriveResult(GameSession session, Long userId) {
		Long winnerId = session.getWinnerId();
		if (winnerId == null) return GameResult.DRAW;
		return winnerId.equals(userId) ? GameResult.WIN : GameResult.LOSS;
	}

	private String lookupWizardClass(Long userId, Long gameSessionId) {
		Player player = playerRepository.findByUserIdAndGameSessionId(userId, gameSessionId);
		if (player == null || player.getWizardClass() == null) return null;
		return player.getWizardClass().name();
	}

	public void logoutUser(String token) {
		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
		}
		user.setStatus(UserStatus.OFFLINE);
		user.setToken(null);
		userRepository.saveAndFlush(user);
	}
}
