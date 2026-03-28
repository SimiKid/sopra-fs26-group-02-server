package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceTest {
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AuthenticationService authenticationService;

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
    public void loginUser_validInputs_success() {
        // given
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        // when
        User loggedInUser = authenticationService.loginUser(testUser);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(Mockito.any());

        assertEquals(testUser.getId(), loggedInUser.getId());
        assertEquals(testUser.getUsername(), loggedInUser.getUsername());
    }

    @Test
    public void loginUser_wrongPassword_throwsException() {
        // given
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        // when -> attempt to login with wrong password
        User loginAttempt = new User();
        loginAttempt.setUsername("testUsername");
        loginAttempt.setPassword("wrongPassword");

        // then
        assertThrows(ResponseStatusException.class, () -> authenticationService.loginUser(loginAttempt));
    }

    @Test
    public void loginUser_userNotFound_throwsException() {
        // given
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

        // when -> attempt to login with non-existing user
        testUser.setUsername("nonExistingUser");

        // then
        assertThrows(ResponseStatusException.class, () -> authenticationService.loginUser(testUser));
    }   
}

