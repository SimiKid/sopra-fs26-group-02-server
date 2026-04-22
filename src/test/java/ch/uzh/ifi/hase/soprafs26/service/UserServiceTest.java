
package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.GameResult;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.Location;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameHistoryEntryDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private GameSessionRepository gameSessionRepository;

	@Mock
	private PlayerRepository playerRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setUsername("testUsername");
		testUser.setPassword("testPassword");

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {
		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		// given -> a first user has already been created
		userService.createUser(testUser);

		// when -> setup additional mocks for UserRepository
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// then -> attempt to create second user with same user -> check that an error
		// is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void createUser_blankInputs_throwsException() {
		// given -> a first user has already been created
		testUser.setUsername("");

		// then
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void getGameHistory_emptyHistory_returnsEmptyList() {
		Mockito.when(gameSessionRepository.findFinishedGamesForUser(1L))
			.thenReturn(Collections.emptyList());

		List<GameHistoryEntryDTO> result = userService.getGameHistory(1L);

		assertTrue(result.isEmpty());
	}

	@Test
	public void getGameHistory_winLossDraw_mappedCorrectlyAndPreservesOrder() {
		GameSession won = historySession(10L, 1L, 2L, 1L, LocalDateTime.of(2026, 4, 3, 12, 0));
		GameSession lost = historySession(11L, 1L, 3L, 3L, LocalDateTime.of(2026, 4, 2, 12, 0));
		GameSession drawn = historySession(12L, 4L, 1L, null, LocalDateTime.of(2026, 4, 1, 12, 0));

		Mockito.when(gameSessionRepository.findFinishedGamesForUser(1L))
			.thenReturn(List.of(won, lost, drawn));
		Mockito.when(playerRepository.findByUserIdAndGameSessionId(Mockito.anyLong(), Mockito.anyLong()))
			.thenReturn(null);

		List<GameHistoryEntryDTO> result = userService.getGameHistory(1L);

		assertEquals(3, result.size());
		assertEquals(GameResult.WIN, result.get(0).getResult());
		assertEquals(GameResult.LOSS, result.get(1).getResult());
		assertEquals(GameResult.DRAW, result.get(2).getResult());
	}

	@Test
	public void getGameHistory_includesWizardClassesAndWeather() {
		GameSession session = historySession(20L, 1L, 2L, 1L, LocalDateTime.of(2026, 4, 5, 12, 0));
		session.setArenaLocation(Location.ZURICH);
		session.setTemperature(TemperatureCategory.HOT);
		session.setRain(RainCategory.RAINING);

		Player me = new Player();
		me.setUserId(1L);
		me.setGameSessionId(20L);
		me.setWizardClass(WizardClass.ATTACKWIZARD);

		Player opp = new Player();
		opp.setUserId(2L);
		opp.setGameSessionId(20L);
		opp.setWizardClass(WizardClass.TANKWIZARD);

		Mockito.when(gameSessionRepository.findFinishedGamesForUser(1L)).thenReturn(List.of(session));
		Mockito.when(playerRepository.findByUserIdAndGameSessionId(1L, 20L)).thenReturn(me);
		Mockito.when(playerRepository.findByUserIdAndGameSessionId(2L, 20L)).thenReturn(opp);

		GameHistoryEntryDTO entry = userService.getGameHistory(1L).get(0);

		assertEquals("Zurich", entry.getLocation());
		assertEquals("ATTACKWIZARD", entry.getMyWizardClass());
		assertEquals("TANKWIZARD", entry.getOpponentWizardClass());
		assertEquals(TemperatureCategory.HOT, entry.getTemperature());
		assertEquals(RainCategory.RAINING, entry.getRain());
		assertEquals(LocalDateTime.of(2026, 4, 5, 12, 0), entry.getGameDate());
	}

	@Test
	public void logoutUser_validToken_clearsTokenAndSetsOffline() {
		User user = new User();
		user.setId(42L);
		user.setToken("valid-token");
		user.setStatus(UserStatus.ONLINE);

		Mockito.when(userRepository.findByToken("valid-token")).thenReturn(user);

		userService.logoutUser("valid-token");

		assertNull(user.getToken());
		assertEquals(UserStatus.OFFLINE, user.getStatus());
		Mockito.verify(userRepository).saveAndFlush(user);
	}

	@Test
	public void logoutUser_invalidToken_throwsUnauthorized() {
		Mockito.when(userRepository.findByToken("bad-token")).thenReturn(null);

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
			() -> userService.logoutUser("bad-token"));

		assertEquals(401, ex.getStatusCode().value());
	}

	private GameSession historySession(Long id, Long player1Id, Long player2Id, Long winnerId, LocalDateTime createdAt) {
		GameSession s = new GameSession();
		s.setId(id);
		s.setPlayer1Id(player1Id);
		s.setPlayer2Id(player2Id);
		s.setWinnerId(winnerId);
		s.setCreatedAt(createdAt);
		s.setGameStatus(GameStatus.FINISHED);
		return s;
	}
}
