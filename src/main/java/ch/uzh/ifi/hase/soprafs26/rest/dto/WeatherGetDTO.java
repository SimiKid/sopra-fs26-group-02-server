package ch.uzh.ifi.hase.soprafs26.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;

@Schema(description = "DTO returned when retrieving weather information")
public class WeatherGetDTO {

    @Schema(description = "Current rain category", example = "RAINING")
    private RainCategory rainCategory;

    @Schema(description = "Current temperature category", example = "HOT")
    private TemperatureCategory temperatureCategory;

    public RainCategory getRainCategory() {
        return rainCategory;
    }

    public void setRainCategory(RainCategory rain) {
        this.rainCategory = rain;
    }

    public TemperatureCategory getTemperatureCategory() {
        return temperatureCategory;
    }   

    public void setTemperatureCategory(TemperatureCategory temperature) {
        this.temperatureCategory = temperature;
    }
}
