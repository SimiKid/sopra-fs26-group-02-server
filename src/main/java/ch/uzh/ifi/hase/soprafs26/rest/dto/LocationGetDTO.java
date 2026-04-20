package ch.uzh.ifi.hase.soprafs26.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO returned when retrieving location")
public class LocationGetDTO {

    @Schema(description = "Location name", example = "Zurich")
    private String locationName;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}