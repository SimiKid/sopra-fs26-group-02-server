package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestHeader;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameHistoryEntryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;


/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */


@RestController
@Tag(name = "User", description = "User registration and authentication endpoints")
public class UserController {

	private final UserService userService;
	private final AuthenticationService authenticationService;

	UserController(UserService userService, AuthenticationService authenticationService) {
		this.userService = userService;
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@Operation(summary = "Register a new user", description = "Creates a new user account with the provided username and password")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "User successfully created"),
		@ApiResponse(responseCode = "409", description = "Username already exists"),
		@ApiResponse(responseCode = "400", description = "Invalid input")
	})
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

	@PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
	@Operation(summary = "Login a user", description = "Authenticates a user with username and password and returns a session token")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Login successful"),
		@ApiResponse(responseCode = "401", description = "Invalid credentials")
	})
    public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User loggedUser = authenticationService.loginUser(userInput);

        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedUser);
    }

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(summary = "Logout a user", description = "Logs out the authenticated user and invalidates the session token")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Logout successful"),
		@ApiResponse(responseCode = "401", description = "Missing or invalid token")
	})
	public void logoutUser(@RequestHeader("Authorization") String token) {
		userService.logoutUser(token);
	}

	@GetMapping("/users/me/games")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Operation(summary = "Get my game history", description = "Returns the authenticated user's finished games, most recent first")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Game history returned"),
		@ApiResponse(responseCode = "401", description = "Missing or invalid token")
	})
	public List<GameHistoryEntryDTO> getMyGameHistory(@RequestHeader("Authorization") String token) {
		User user = authenticationService.authenticateByToken(token);
		return userService.getGameHistory(user.getId());
	}
}
