package ch.uzh.ifi.hase.soprafs26.constant;

import java.util.Map;

public class WeatherModifier {
    public static final Map<Element, Map<Object, Double>> ELEMENT_MODIFIERS = Map.of(
        Element.FIRE, Map.of(
            TemperatureCategory.COLD, 0.8,
            TemperatureCategory.NEUTRAL, 1.0,
            TemperatureCategory.HOT, 1.2,
            RainCategory.RAINING, 0.8,
            RainCategory.CLEAR, 1.2
        ),
        Element.ICE, Map.of(
            TemperatureCategory.COLD, 1.2,
            TemperatureCategory.NEUTRAL, 1.0,
            TemperatureCategory.HOT, 0.8,
            RainCategory.RAINING, 1.2,
            RainCategory.CLEAR, 0.8
        ),
        Element.LIGHTNING, Map.of(
            TemperatureCategory.COLD, 1.0,
            TemperatureCategory.NEUTRAL, 1.0,
            TemperatureCategory.HOT, 1.0,
            RainCategory.RAINING, 1.4,
            RainCategory.CLEAR, 0.6
        ),
        Element.NEUTRAL, Map.of(
            TemperatureCategory.COLD, 0.8,
            TemperatureCategory.NEUTRAL, 1.4,
            TemperatureCategory.HOT, 0.8,
            RainCategory.RAINING, 1.0,
            RainCategory.CLEAR, 1.0
        ));
}