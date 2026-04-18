package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.ifi.hase.soprafs26.constant.Location;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final Logger log = LoggerFactory.getLogger(WeatherService.class);

    public WeatherGetDTO getWeatherForLocation(Location location) {
        // fetch weather data from external API
        // if call fails, return default weather data (clear and neutral)
        // processes and returns weather data

        RainCategory rainCategory;
        TemperatureCategory temperatureCategory;

        try {
            List<Integer> weatherData = fetchWeatherFromAPI(location);
            rainCategory = categorizeRain(weatherData.get(0));
            temperatureCategory = categorizeTemperature(weatherData.get(1));
        } catch (Exception e) {
            log.debug("Failed to fetch weather data for location: " + location, e + ", Returning default weather.");
            return defaultWeather();
        }

        WeatherGetDTO weatherDTO = new WeatherGetDTO();
        weatherDTO.setRainCategory(rainCategory);
        weatherDTO.setTemperatureCategory(temperatureCategory);
        return weatherDTO;
    }

    private WeatherGetDTO defaultWeather() {
        WeatherGetDTO weatherDTO = new WeatherGetDTO();
        weatherDTO.setRainCategory(RainCategory.CLEAR);
        weatherDTO.setTemperatureCategory(TemperatureCategory.NEUTRAL);
        return weatherDTO;
    }

    private List<Integer> fetchWeatherFromAPI(Location location) {
        // Implement API call to fetch weather data based on location
        float lat = location.getLatitude();
        float lon = location.getLongitude();
        // call: https://api.openweathermap.org/data/3.0/onecall?lat={location.getLatitude()}&lon={location.getLongitude()}&exclude={part}&appid={API key} 

        // Return the weather data as a list of integers (rain and temperature)
        int rain = 0; // Placeholder for actual API call result
        int temperature = 20; // Placeholder for actual API call result
        return Arrays.asList(rain, temperature); // Placeholder for actual API call
    }

    private RainCategory categorizeRain(int rain) {
        return rain > 1 ? RainCategory.RAINING : RainCategory.CLEAR;
    }

    private TemperatureCategory categorizeTemperature(int temperature) {
        if (temperature > 20) {
            return TemperatureCategory.HOT;
        } else if (temperature < 5) {
            return TemperatureCategory.COLD;
        } else {
            return TemperatureCategory.NEUTRAL;
        }
    }
}
