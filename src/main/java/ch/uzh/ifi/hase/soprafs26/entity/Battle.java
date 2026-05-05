package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "battles")
public class Battle implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Integer turnNumber;

    @Column(nullable = false)
    private Long gameId;

    @Column(nullable = false)
    private Long player1Id;

    @Column(nullable = false)
    private Long player2Id;

    private Long activePlayerId;

    @Enumerated(EnumType.STRING)
    private Attack  currentAction;

    private int damageDealt;

    private LocalDateTime timeStamp;

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(Integer turnNumber) {
        this.turnNumber = turnNumber;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
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

    public Attack getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Attack currentAction) {
        this.currentAction = currentAction;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

}
