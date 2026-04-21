package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import ch.uzh.ifi.hase.soprafs26.Interceptor.AuthInterceptor;
import ch.uzh.ifi.hase.soprafs26.constant.GameResult;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameHistoryEntryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

    @MockitoBean
        private AuthInterceptor authInterceptor;

	@MockitoBean
	private AuthenticationService authenticationService;

	@MockitoBean	
	private UserService userService;

	@BeforeEach
    void setup() {
        Mockito.when(authInterceptor.preHandle(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(true);
	}

	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setPassword("testPassword");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("testPassword");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}


	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}

	@Test
	public void createUser_invalidInput_duplicateUsername() throws Exception {

		// given
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("testPassword");

		given(userService.createUser(Mockito.any()))
		.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username is not unique."));

		// when
		MockHttpServletRequestBuilder postRequest = post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isConflict());
	}

	@Test
	public void createUser_invalidInput_emptyUsername() throws Exception {
		// given
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("");
		userPostDTO.setPassword("testPassword");

		given(userService.createUser(Mockito.any()))
		.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password must not be empty"));

		// when
		MockHttpServletRequestBuilder postRequest = post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	@Test
	public void loginUser_validInput_userLoggedIn() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setPassword("testPassword");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("testPassword");

		given(authenticationService.loginUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.token", is(user.getToken())));
	}

	@Test
	public void loginUser_invalidcredential() throws Exception {
		// given
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("testPassword");

		given(authenticationService.loginUser(Mockito.any()))
		.willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

		// when
		MockHttpServletRequestBuilder postRequest = post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void getMyGameHistory_validToken_returnsEntries() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setToken("valid-token");

		GameHistoryEntryDTO entry = new GameHistoryEntryDTO();
		entry.setGameDate(LocalDateTime.of(2026, 4, 5, 12, 0));
		entry.setLocation("Zurich");
		entry.setResult(GameResult.WIN);
		entry.setMyWizardClass("ATTACKWIZARD");
		entry.setOpponentWizardClass("TANKWIZARD");

		given(authenticationService.authenticateByToken("valid-token")).willReturn(user);
		given(userService.getGameHistory(1L)).willReturn(List.of(entry));

		mockMvc.perform(get("/users/me/games").header("Authorization", "valid-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()", is(1)))
				.andExpect(jsonPath("$[0].location", is("Zurich")))
				.andExpect(jsonPath("$[0].result", is("WIN")))
				.andExpect(jsonPath("$[0].myWizardClass", is("ATTACKWIZARD")))
				.andExpect(jsonPath("$[0].opponentWizardClass", is("TANKWIZARD")));
	}

	@Test
	public void getMyGameHistory_invalidToken_returnsUnauthorized() throws Exception {
		given(authenticationService.authenticateByToken("bad-token"))
				.willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

		mockMvc.perform(get("/users/me/games").header("Authorization", "bad-token"))
				.andExpect(status().isUnauthorized());
	}

}