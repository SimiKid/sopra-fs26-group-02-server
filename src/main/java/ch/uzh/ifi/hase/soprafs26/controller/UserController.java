package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;


/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */


@RestController
public class UserController {

	private final UserService userService;
	private final AuthenticationService authenticationService;

	UserController(UserService userService, AuthenticationService authenticationService) {
		this.userService = userService;
		this.authenticationService = authenticationService;
	}

/* template code, may be deleted in the future if not needed
@GetMapping("/users")
@ResponseStatus(HttpStatus.OK)
@ResponseBody
public List<UserGetDTO> getAllUsers() {
	// fetch all users in the internal representation
	List<User> users = userService.getUsers();
	List<UserGetDTO> userGetDTOs = new ArrayList<>();

	// convert each user to the API representation
	for (User user : users) {
		userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
	}
	return userGetDTOs;
}
*/


	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
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
    public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
        User loggedUser = authenticationService.loginUser(userInput);

        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedUser);
    }

	// Helper for protected endpoints (Task #76 - Session Management):
	// To secure a endpoint, follow these two steps:
	// 1. Add '@RequestHeader("Authorization") String token' as a method parameter.
	// 2. Call 'authenticationService.authenticateByToken(token);' as the first line of the method.
	// If the token is invalid or missing, an UNAUTHORIZED (401) exception will be thrown automatically.
}
