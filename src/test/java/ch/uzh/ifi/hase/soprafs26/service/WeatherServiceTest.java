package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.Location;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.rest.dto.WeatherGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class WeatherServiceTest {

    private WeatherService weatherService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        weatherService = new WeatherService();

        ReflectionTestUtils.setField(weatherService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(weatherService, "restTemplate", restTemplate);
    }

    @Test
    void getWeatherForLocation_hotAndRaining_mapsToCategories() {
        String apiResponse = "{\"main\":{\"temp\":24.2,\"feels_like\":23.0},\"rain\":{\"1h\":2.3}}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(apiResponse);

        WeatherGetDTO result = weatherService.getWeatherForLocation(Location.ZURICH);

        assertEquals(RainCategory.RAINING, result.getRainCategory());
        assertEquals(TemperatureCategory.HOT, result.getTemperatureCategory());
    }

    @Test
    void getWeatherForLocation_noRainAndNeutralTemp_mapsToClearAndNeutral() {
        String apiResponse = "{\"main\":{\"temp\":15.0,\"pressure\":1015},\"clouds\":{\"all\":100}}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(apiResponse);

        WeatherGetDTO result = weatherService.getWeatherForLocation(Location.LONDON);

        assertEquals(RainCategory.CLEAR, result.getRainCategory());
        assertEquals(TemperatureCategory.NEUTRAL, result.getTemperatureCategory());
    }

    @Test
    void getWeatherForLocation_apiCallFails_returnsFallbackWeather() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
            .thenThrow(new RuntimeException("OpenWeather down"));

        WeatherGetDTO result = weatherService.getWeatherForLocation(Location.TOKYO);

        assertNotNull(result.getRainCategory());
        assertNotNull(result.getTemperatureCategory());
    }

    @Test
    void getWeatherForLocation_buildsUrlWithCoordinatesAndApiKey() {
        String apiResponse = "{\"main\":{\"temp\":9.0,\"pressure\":1015}}";
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        when(restTemplate.getForObject(urlCaptor.capture(), eq(String.class))).thenReturn(apiResponse);

        weatherService.getWeatherForLocation(Location.ZURICH);

        String url = urlCaptor.getValue();
        assertTrue(url.contains("lat=47.38"));
        assertTrue(url.contains("lon=8.55"));
        assertTrue(url.contains("appid=test-api-key"));
        assertTrue(url.contains("units=metric"));
    }
}