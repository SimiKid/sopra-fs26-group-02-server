package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.GameResult;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;

public class GameHistoryEntryDTO {

    private LocalDateTime gameDate;
    private String location;
    private GameResult result;
    private String myWizardClass;
    private String opponentWizardClass;
    private TemperatureCategory temperature;
    private RainCategory rain;

    public LocalDateTime getGameDate() { return gameDate; }
    public void setGameDate(LocalDateTime gameDate) { this.gameDate = gameDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public GameResult getResult() { return result; }
    public void setResult(GameResult result) { this.result = result; }

    public String getMyWizardClass() { return myWizardClass; }
    public void setMyWizardClass(String myWizardClass) { this.myWizardClass = myWizardClass; }

    public String getOpponentWizardClass() { return opponentWizardClass; }
    public void setOpponentWizardClass(String opponentWizardClass) { this.opponentWizardClass = opponentWizardClass; }

    public TemperatureCategory getTemperature() { return temperature; }
    public void setTemperature(TemperatureCategory temperature) { this.temperature = temperature; }

    public RainCategory getRain() { return rain; }
    public void setRain(RainCategory rain) { this.rain = rain; }
}
