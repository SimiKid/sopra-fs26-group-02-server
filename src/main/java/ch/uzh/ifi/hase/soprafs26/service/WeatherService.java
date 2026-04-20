package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import ch.uzh.ifi.hase.soprafs26.constant.Location;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import ch.uzh.ifi.hase.soprafs26.entity.WeatherData;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class WeatherService {

    private final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Value("${openweather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Random RANDOM = new Random();
    private static final int RAINSIZE = RainCategory.values().length;
    private static final int TEMPSIZE = TemperatureCategory.values().length;

    public WeatherGetDTO getWeatherForLocation(Location location) {
        // fetch weather data from external API
        // if call fails, return random weather data
        // processes and returns weather data

        RainCategory rainCategory;
        TemperatureCategory temperatureCategory;

        try {
            WeatherData weatherData = fetchWeatherFromAPI(location);
            rainCategory = categorizeRain(weatherData.getRain());  // Clear and explicit
            temperatureCategory = categorizeTemperature(weatherData.getTemperature());

        } catch (Exception e) {
            log.error("Weather API call failed for location {}", location, e);
            return fallbackWeather();
        }

        WeatherGetDTO weatherDTO = new WeatherGetDTO();
        weatherDTO.setRainCategory(rainCategory);
        weatherDTO.setTemperatureCategory(temperatureCategory);
        return weatherDTO;
    }

    private WeatherGetDTO fallbackWeather() {
        WeatherGetDTO weatherDTO = new WeatherGetDTO();
        // random values from the enums
        weatherDTO.setRainCategory(RainCategory.values()[RANDOM.nextInt(RAINSIZE)]);
        weatherDTO.setTemperatureCategory(TemperatureCategory.values()[RANDOM.nextInt(TEMPSIZE)]);
        return weatherDTO;
    }

    private WeatherData fetchWeatherFromAPI(Location location) {
        float lat = location.getLatitude();
        float lon = location.getLongitude();

        // building url for api call
        String url = UriComponentsBuilder
                .fromUriString("https://api.openweathermap.org/data/2.5/weather")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .toUriString();

        // log.info("Fetching weather data from API for location {}: {}", location, url);

        // saving response as string instead of JsonNode because Spring Boot 4 overrides jackson-databind to version 3, which causes issues with JsonNode.
        String response = restTemplate.getForObject(url, String.class);
        // log.info("RAW API RESPONSE: {}", response);
        
        WeatherData weatherData = new WeatherData();
        double temp = 0;
        double rain = 0;

        if (response.contains("\"temp\"")) {
            String tempStr = response.split("\"temp\":")[1].split(",")[0];
            temp = Double.parseDouble(tempStr);
        }
        else {
            temp = 15; // default value if temp is not found in response
        }

        if (response.contains("\"rain\"")) {
            String rainStr = response.split("\"1h\":")[1].split("}")[0];
            rain = Double.parseDouble(rainStr);
        }
        else {
            rain = 0; // default value if rain is not found in response
        }


        weatherData.setRain(rain);
        weatherData.setTemperature(temp);
        return weatherData;
    }

    private RainCategory categorizeRain(double rain) {
        return rain > 0 ? RainCategory.RAINING : RainCategory.CLEAR;    }

    private TemperatureCategory categorizeTemperature(double temperature) {
        if (temperature > 20) {
            return TemperatureCategory.HOT;
        } else if (temperature < 10) {
            return TemperatureCategory.COLD;
        } else {
            return TemperatureCategory.NEUTRAL;
        }
    }
}
