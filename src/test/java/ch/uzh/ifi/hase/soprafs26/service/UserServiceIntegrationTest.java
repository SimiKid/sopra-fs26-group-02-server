package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameResult;
import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameHistoryEntryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises UserService end-to-end against an in-memory H2 database.
 * Focus: behaviour that only surfaces with a real persistence layer —
 * the unique-username constraint, token lookup, and the JPQL-backed
 * getGameHistory query with its WIN/LOSS/DRAW derivation.
 */
@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired private UserRepository userRepository;
    @Autowired private GameSessionRepository gameSessionRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private UserService userService;

    @Test
    public void createUser_persistsWithTokenAndOnlineStatus() {
        User input = new User();
        input.setUsername("alice");
        input.setPassword("pw");

        User created = userService.createUser(input);

        assertNotNull(created.getId());
        assertNotNull(created.getToken());
        assertEquals(UserStatus.ONLINE, created.getStatus());
        assertNotNull(created.getCreationDate());
        assertNotNull(userRepository.findByUsername("alice"));
        assertEquals(created.getId(), userRepository.findByToken(created.getToken()).getId());
    }

    @Test
    public void createUser_duplicateUsername_conflict() {
        User first = new User();
        first.setUsername("bob");
        first.setPassword("pw");
        userService.createUser(first);

        User second = new User();
        second.setUsername("bob");
        second.setPassword("other");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(second));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    public void createUser_blankPassword_badRequest() {
        User input = new User();
        input.setUsername("carol");
        input.setPassword("   ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.createUser(input));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    public void logoutUser_clearsTokenAndMarksOffline() {
        User input = new User();
        input.setUsername("dave");
        input.setPassword("pw");
        User created = userService.createUser(input);
        String token = created.getToken();

        userService.logoutUser(token);

        User reloaded = userRepository.findById(created.getId()).orElseThrow();
        assertNull(reloaded.getToken());
        assertEquals(UserStatus.OFFLINE, reloaded.getStatus());
        assertNull(userRepository.findByToken(token));
    }

    @Test
    public void logoutUser_invalidToken_unauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.logoutUser("not-a-real-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void getGameHistory_returnsWinLossDrawForUserAcrossFinishedSessions() {
        User me = createUser("me", "pw");
        User foe = createUser("foe", "pw");

        // Three finished sessions: one I won, one I lost, one draw.
        seedFinishedSession(me, foe, me.getId(), WizardClass.ATTACKWIZARD, WizardClass.TANKWIZARD);
        seedFinishedSession(me, foe, foe.getId(), WizardClass.BALANCEDWIZARD, WizardClass.ATTACKWIZARD);
        seedFinishedSession(me, foe, null, WizardClass.TANKWIZARD, WizardClass.BALANCEDWIZARD);

        // An unrelated finished session between two other users must not appear.
        User stranger1 = createUser("s1", "pw");
        User stranger2 = createUser("s2", "pw");
        seedFinishedSession(stranger1, stranger2, stranger1.getId(), WizardClass.ATTACKWIZARD, WizardClass.TANKWIZARD);

        // A still-in-progress session between me and foe must not appear.
        seedSession(me, foe, GameStatus.BATTLE, null, null, null);

        List<GameHistoryEntryDTO> history = userService.getGameHistory(me.getId());

        assertEquals(3, history.size());
        long wins = history.stream().filter(h -> h.getResult() == GameResult.WIN).count();
        long losses = history.stream().filter(h -> h.getResult() == GameResult.LOSS).count();
        long draws = history.stream().filter(h -> h.getResult() == GameResult.DRAW).count();
        assertEquals(1, wins);
        assertEquals(1, losses);
        assertEquals(1, draws);

        GameHistoryEntryDTO win = history.stream()
                .filter(h -> h.getResult() == GameResult.WIN).findFirst().orElseThrow();
        assertEquals(WizardClass.ATTACKWIZARD.name(), win.getMyWizardClass());
        assertEquals(WizardClass.TANKWIZARD.name(), win.getOpponentWizardClass());
    }

    // -- helpers --

    private User createUser(String username, String password) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        return userService.createUser(u);
    }

    private void seedFinishedSession(User p1, User p2, Long winnerId,
                                     WizardClass p1Class, WizardClass p2Class) {
        seedSession(p1, p2, GameStatus.FINISHED, winnerId, p1Class, p2Class);
    }

    private void seedSession(User p1, User p2, GameStatus status, Long winnerId,
                             WizardClass p1Class, WizardClass p2Class) {
        GameSession s = new GameSession();
        s.setPlayer1Id(p1.getId());
        s.setPlayer2Id(p2.getId());
        s.setGameStatus(status);
        s.setWinnerId(winnerId);
        s.setGameCode(randomCode());
        s.setCreatedAt(LocalDateTime.now());
        GameSession saved = gameSessionRepository.saveAndFlush(s);

        if (p1Class != null) savePlayer(p1.getId(), saved.getId(), p1Class);
        if (p2Class != null) savePlayer(p2.getId(), saved.getId(), p2Class);
    }

    private void savePlayer(Long userId, Long sessionId, WizardClass wc) {
        Player p = new Player();
        p.setUserId(userId);
        p.setGameSessionId(sessionId);
        p.setWizardClass(wc);
        p.setHp(100);
        p.setReady(false);
        playerRepository.save(p);
    }

    private String randomCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
