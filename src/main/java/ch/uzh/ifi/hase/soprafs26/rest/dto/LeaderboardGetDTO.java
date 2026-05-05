package ch.uzh.ifi.hase.soprafs26.rest.dto;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO returned when retrieving leaderboard information")
public class LeaderboardGetDTO {
    @Schema(description = "Username of the user", example = "johndoe")
    private String username;
    @Schema(description = "Total number of games of the user", example = "15")
    private int totalGames;
    @Schema(description = "Number of wins of the user", example = "10")
    private int wins;
    @Schema(description = "Number of losses of the user", example = "5")
    private int losses;
    @Schema(description = "Win rate of the user", example = "0.6666666666666666")
    private float winRate;

    public void setUsername(String username){
        this.username = username;
    }

    public void setTotalGames(int totalGames){
        this.totalGames = totalGames;
    }

    public void setWins(int wins){
        this.wins = wins;
    }

    public void setLosses(int losses){
        this.losses = losses;
    }

    public void setWinRate(float winRate){
        this.winRate = winRate;
    }

    public String getUsername(){
        return username;
    }
    
    public int getTotalGames(){
        return totalGames;
    }

    public int getWins(){
        return wins;
    }
    
    public int getLosses(){
        return losses;
    }
    
    public float getWinRate(){
        return winRate;
    }
}
