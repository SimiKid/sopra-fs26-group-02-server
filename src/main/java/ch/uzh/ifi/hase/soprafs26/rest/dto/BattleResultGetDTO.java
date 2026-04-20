package ch.uzh.ifi.hase.soprafs26.rest.dto;


public class BattleResultGetDTO {
    private Long winnerUserId;
    private Long loserUserId;
    private int totalDamageDealt;
    private int turnsPlayed;
    private WeatherGetDTO weather;

    public Long getWinnerUserId(){
        return winnerUserId;
    }

    public void setWinnerUserId(Long winnerUserId){
        this.winnerUserId = winnerUserId;
    }
    
    public Long getLoserUserId(){
        return loserUserId;
    }

    public void setLoserUserId(Long loserUserId){
        this.loserUserId = loserUserId;
    }

    public int getTotalDamageDealt(){
        return totalDamageDealt;
    }

    public void setTotalDamageDealt(int totalDamageDealt){
        this.totalDamageDealt = totalDamageDealt;
    }

    public int getTurnsPlayed(){
        return turnsPlayed;
    }

    public void setTurnsPlayed(int turnsPlayed){
        this.turnsPlayed = turnsPlayed;
    }

    public WeatherGetDTO getWeather(){
        return weather;
    }

    public void setWeather(WeatherGetDTO weather){
        this.weather = weather;
    }


    
}
