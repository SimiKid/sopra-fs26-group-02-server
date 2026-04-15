package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "DTO returned when retrieving game session information")
public class GameSessionGetDTO {

    @Schema(description = "Unique code used to join the game", example = "abc123ef")
    private String gameCode;

    @Schema(description = "Current status of the game", example = "WAITING")
    private GameStatus gameStatus;

    @Schema(description = "User ID of player 1 (game creator)", example = "1")
    private Long player1Id;

    @Schema(description = "User ID of player 2 (joined player)", example = "2")
    private Long player2Id;

    @Schema(description = "User ID of the player whose turn it is", example = "1")
    private Long activePlayerId;

    @Schema(description = "Timestamp when the game session was created", example = "2026-04-09T14:30:00")
    private LocalDateTime createdAt;

    private Long id;

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Long getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(Long player1Id) {
        this.player1Id = player1Id;
    }

    public Long getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(Long player2Id) {
        this.player2Id = player2Id;
    }

    public Long getActivePlayerId() {
        return activePlayerId;
    }

    public void setActivePlayerId(Long activePlayerId) {
        this.activePlayerId = activePlayerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id; 

    }
}
