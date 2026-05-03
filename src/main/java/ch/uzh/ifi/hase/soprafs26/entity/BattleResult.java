package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;

public class BattleResult {

    private Long winnerUserId;
    private Long loserUserId;
    private int totalDamageDealt;
    private int turnsPlayed;
    private RainCategory rain;
    private TemperatureCategory temperature;

    public Long getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(Long winnerUserId) { this.winnerUserId = winnerUserId; }

    public Long getLoserUserId() { return loserUserId; }
    public void setLoserUserId(Long loserUserId) { this.loserUserId = loserUserId; }

    public int getTotalDamageDealt() { return totalDamageDealt; }
    public void setTotalDamageDealt(int totalDamageDealt) { this.totalDamageDealt = totalDamageDealt; }

    public int getTurnsPlayed() { return turnsPlayed; }
    public void setTurnsPlayed(int turnsPlayed) { this.turnsPlayed = turnsPlayed; }

    public RainCategory getRain() { return rain; }
    public void setRain(RainCategory rain) { this.rain = rain; }

    public TemperatureCategory getTemperature() { return temperature; }
    public void setTemperature(TemperatureCategory temperature) { this.temperature = temperature; }
}