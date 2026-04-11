package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO returned when retrieving user information")
public class UserGetDTO {

	@Schema(description = "Unique identifier of the user", example = "1")
	private Long id;

	@Schema(description = "Username of the user", example = "johndoe")
	private String username;

	@Schema(description = "Current online status of the user", example = "ONLINE")
	private UserStatus status;

	@Schema(description = "Authentication token for the session", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
	private String token;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}


}
