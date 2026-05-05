package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.*;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String gameCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus gameStatus;

    @Column(nullable = false)
    private Long player1Id;

    @Column(nullable = true)
    private Long player2Id;

    @Enumerated(EnumType.STRING)
    private Location arenaLocation;

    @Enumerated(EnumType.STRING)
    private TemperatureCategory temperature;

    @Enumerated(EnumType.STRING)
    private RainCategory rain;

    private Long activePlayerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime startedAt;

    @Column(nullable = true)
    private Integer currentTurnNumber;

    private Long winnerId;

    private boolean player1WantsRematch = false;

    private boolean player2WantsRematch = false;

    @Column(nullable = true, length = 6)
    private String rematchGameCode;

    // getters and setters
        public Long getId() 
            { return id; }
        
        public void setId(Long id) 
            { this.id = id; }

        public String getGameCode() 
        
            { return gameCode; }

        public void setGameCode(String gameCode) 
            { this.gameCode = gameCode; }

        public Long getPlayer1Id() 
            { return player1Id; }

        public void setPlayer1Id(Long player1Id)
            { this.player1Id = player1Id; }

        public Long getPlayer2Id()
            { return player2Id; }

        public void setPlayer2Id(Long player2Id)
            { this.player2Id = player2Id; }

        public Long getActivePlayerId()
            { return activePlayerId; }

        public void setActivePlayerId(Long activePlayerId) 
            { this.activePlayerId = activePlayerId; }

        public Location getArenaLocation() 
            { return arenaLocation; }

        public void setArenaLocation(Location arenaLocation) 
            { this.arenaLocation = arenaLocation; }

        public TemperatureCategory getTemperature() 
            { return temperature; }

        public void setTemperature(TemperatureCategory temperature) 
            { this.temperature = temperature; }
        
        public RainCategory getRain() 
            { return rain; }
        
        public void setRain(RainCategory rain) 
            { this.rain = rain; }

        public GameStatus getGameStatus() 
            { return gameStatus; }

        public void setGameStatus(GameStatus gameStatus) 
            { this.gameStatus = gameStatus; }

        public LocalDateTime getCreatedAt() 
            { return createdAt; }

        public void setCreatedAt(LocalDateTime createdAt) 
            { this.createdAt = createdAt; }

        public Long getWinnerId()
            { return winnerId; }
        
        public void setWinnerId(Long winnerId) 
            { this.winnerId = winnerId; }

        public boolean getPlayer1WantsRematch(){
            return player1WantsRematch;
        }

        public void setPlayer1WantsRematch(boolean player1WantsRematch){
            this.player1WantsRematch = player1WantsRematch;
        }

        public boolean getPlayer2WantsRematch(){
            return player2WantsRematch;
        }

        public void setPlayer2WantsRematch(boolean player2WantsRematch){
            this.player2WantsRematch = player2WantsRematch;
        }

        public String getRematchGameCode(){
            return rematchGameCode;
        }

        public void setRematchGameCode(String rematchGameCode){
            this.rematchGameCode = rematchGameCode;
        }

        public LocalDateTime getStartedAt(){
            return startedAt;
        }

        public void setStartedAt(LocalDateTime startedAt){
            this.startedAt = startedAt;
        }

        public Integer getCurrentTurnNumber(){
            return currentTurnNumber;
        }

        public void setCurrentTurnNumber(Integer currentTurnNumber){
            this.currentTurnNumber = currentTurnNumber;
        }
}