package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;

public class BattleStateDTO {

    private Long activePlayerId;
    private int player1Hp;
    private int player2Hp;
    private int damageDealt;
    private String attackUsed;
    private GameStatus gameStatus;
    private Long winnerId;

    public Long getActivePlayerId() {
        return activePlayerId; 
    }
    public void setActivePlayerId(Long activePlayerId) { 
        this.activePlayerId = activePlayerId; 
    }

    public int getPlayer1Hp() {
        return player1Hp; 
    }
    public void setPlayer1Hp(int player1Hp) {
        this.player1Hp = player1Hp; 
    }

    public int getPlayer2Hp() {
        return player2Hp; 
    }
    public void setPlayer2Hp(int player2Hp) {
        this.player2Hp = player2Hp; 
    }

    public int getDamageDealt() {
        return damageDealt; 
    }
    public void setDamageDealt(int damageDealt) { 
        this.damageDealt = damageDealt; 
    }

    public String getAttackUsed() {
        return attackUsed; 
    }
    public void setAttackUsed(String attackUsed) {
        this.attackUsed = attackUsed; 
    }

    public GameStatus getGameStatus() {
        return gameStatus; 
    }
    public void setGameStatus(GameStatus gameStatus) { 
        this.gameStatus = gameStatus; 
    }

    public Long getWinnerId() { 
        return winnerId; 
    }
    public void setWinnerId(Long winnerId) { 
        this.winnerId = winnerId; 
    }
}