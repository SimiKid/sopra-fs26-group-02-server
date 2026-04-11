package ch.uzh.ifi.hase.soprafs26.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for user registration and login requests")
public class UserPostDTO {

	@Schema(description = "Desired username", example = "johndoe", requiredMode = Schema.RequiredMode.REQUIRED)
	private String username;

	@Schema(description = "User password", example = "securePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
