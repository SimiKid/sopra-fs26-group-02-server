package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;

public class BattleStateDTO {

    private Long activePlayerId;
    private int player1Hp;
    private int player2Hp;
    private int damageDealt;
    private String attackUsed;
    private GameStatus gameStatus;
    private Long winnerId;

    private Long player1UserId;
    private Long player2UserId;
    private String player1Username;
    private String player2Username;
    private String player1WizardClass;
    private String player2WizardClass;

    private String location;
    private RainCategory rain;
    private TemperatureCategory temperature;

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

    public Long getPlayer1UserId() {
        return player1UserId;
    }

    public void setPlayer1UserId(Long player1UserId) {
        this.player1UserId = player1UserId;
    }

    public Long getPlayer2UserId() {
        return player2UserId;
    }

    public void setPlayer2UserId(Long player2UserId) {
        this.player2UserId = player2UserId;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public String getPlayer1WizardClass() {
        return player1WizardClass;
    }

    public void setPlayer1WizardClass(String player1WizardClass) {
        this.player1WizardClass = player1WizardClass;
    }

    public String getPlayer2WizardClass() {
        return player2WizardClass;
    }

    public void setPlayer2WizardClass(String player2WizardClass) {
        this.player2WizardClass = player2WizardClass;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public RainCategory getRain() {
        return rain;
    }

    public void setRain(RainCategory rain) {
        this.rain = rain;
    }

    public TemperatureCategory getTemperature() {
        return temperature;
    }

    public void setTemperature(TemperatureCategory temperature) {
        this.temperature = temperature;
    }
}