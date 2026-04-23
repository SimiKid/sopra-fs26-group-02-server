package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the login / token / logout round-trip against H2. Focus:
 * the side-effects that only matter when state is actually persisted —
 * token rotation on login (the old token stops authenticating) and token
 * invalidation on logout.
 */
@SpringBootTest
@Transactional
public class AuthenticationServiceIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private AuthenticationService authenticationService;

    @Test
    public void loginUser_validCredentials_rotatesTokenAndMarksOnline() {
        User created = register("alice", "secret");
        String registrationToken = created.getToken();

        // Force offline state so we can observe the transition back to ONLINE.
        userService.logoutUser(registrationToken);

        User loggedIn = authenticationService.loginUser(credentials("alice", "secret"));

        assertEquals(UserStatus.ONLINE, loggedIn.getStatus());
        assertNotNull(loggedIn.getToken());
        // After logout the registration token was cleared, so the new token is
        // necessarily different — but we still guard against a regression where
        // login reuses a stale token.
        assertNotEquals(registrationToken, loggedIn.getToken());
    }

    @Test
    public void loginUser_wrongPassword_unauthorized() {
        register("bob", "secret");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.loginUser(credentials("bob", "wrong")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void loginUser_unknownUsername_unauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.loginUser(credentials("ghost", "pw")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    public void authenticateByToken_validToken_returnsUser() {
        User created = register("carol", "pw");

        User found = authenticationService.authenticateByToken(created.getToken());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    public void authenticateByToken_nullOrEmpty_unauthorized() {
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticateByToken(null)).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticateByToken("")).getStatusCode());
    }

    @Test
    public void logout_invalidatesTokenForSubsequentAuthentication() {
        User created = register("dave", "pw");
        String token = created.getToken();

        userService.logoutUser(token);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authenticationService.authenticateByToken(token));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    // -- helpers --

    private User register(String username, String password) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        return userService.createUser(u);
    }

    private User credentials(String username, String password) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        return u;
    }
}
