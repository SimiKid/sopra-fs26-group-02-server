package ch.uzh.ifi.hase.soprafs26.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for creating a new game session")
public class GameSessionPostDTO {

    @Schema(description = "User ID of the player creating the game", example = "1")
    private Long player1Id;

    public Long getPlayer1Id() {
		return player1Id;
	}

	public void setPlayer1Id(Long player1Id) {
		this.player1Id = player1Id;
	}

}
