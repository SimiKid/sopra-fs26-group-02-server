package ch.uzh.ifi.hase.soprafs26.constant;

public enum Location {

    ZURICH("Zurich", 47.38f, 8.55f),
    NEW_YORK("New York", 40.71f, -74.01f),
    TOKYO("Tokyo", 35.69f, 139.69f),
    SYDNEY("Sydney", -33.877f, 151.21f),
    LONDON("London", 51.51f, -0.1378f),
    PARIS("Paris", 48.86f, 2.35f),
    PEKING("Peking", 39.90f, 116.41f),
    COLOMBO("Colombo", 6.93f, 79.86f),
    CAIRO("Cairo", 30.04f, 31.24f),
    RIO("Rio de Janeiro", -22.91f, -43.17f),
    MONTREAL("Montreal", 45.50f, -73.57f),
    MOSCOW("Moscow", 55.76f, 37.62f),
    BERLIN("Berlin", 52.52f, 13.41f),
    CAPE_TOWN("Cape Town", -33.92f, 18.42f),
    SINGAPORE("Singapore", 1.35f, 103.82f),
    BUDAPEST("Budapest", 47.50f, 19.04f),
    EMBRACH("Embrach", 47.51f, 8.65f),
    SAHARA("Sahara", 21.12f, -11.40f),
    OLTEN("Olten", 47.35f, 7.90f),
    NEERACH("Neerach", 47.45f, 8.65f),
    LAGOS("Lagos", 6.52f, 3.38f),
    BUENOS_AIRES("Buenos Aires", -34.60f, -58.38f),
    MUMBAI("Mumbai", 19.08f, 72.88f),
    NAIROBI("Nairobi", -1.29f, 36.82f),
    HELSINKI("Helsinki", 60.18f, 24.94f),
    DAKAR("Dakar", 14.69f, -17.45f),
    CUSCO("Cusco", -13.53f, -71.97f),
    REYKJAVIK("Reykjavik", 64.15f, -21.94f),
    KABUL("Kabul", 34.53f, 69.17f),
    MCMURDO("McMurdo Station", -77.84f, 166.69f),
    ANCHORAGE("Anchorage", 61.22f, -149.90f),
    HONOLULU("Honolulu", 21.31f, -157.86f),
    NUUK("Nuuk", 64.18f, -51.72f),
    ABUJA("Abuja", 9.08f, 7.49f),
    ACCRA("Accra", 5.56f, -0.20f),
    DUBAI("Dubai", 25.20f, 55.27f),
    ROME("Rome", 41.90f, 12.48f),
    MADRID("Madrid", 40.42f, -3.70f),
    STOCKHOLM("Stockholm", 59.33f, 18.07f),
    OSLO("Oslo", 59.91f, 10.75f),
    VIENNA("Vienna", 48.21f, 16.37f),
    PRAGUE("Prague", 50.08f, 14.43f),
    DUBLIN("Dublin", 53.34f, -6.26f),
    BRUSSELS("Brussels", 50.85f, 4.35f),
    RIYADH("Riyadh", 24.71f, 46.67f),
    TEHRAN("Tehran", 35.68f, 51.38f),
    BAGHDAD("Baghdad", 33.31f, 44.36f),
    JAKARTA("Jakarta", -6.21f, 106.85f),
    NEW_DELHI("New Delhi", 28.61f, 77.21f),
    BANGKOK("Bangkok", 13.75f, 100.50f),
    PERTH("Perth", -31.95f, 115.86f),
    LIMA("Lima", -12.04f, -77.03f),
    SANTIAGO("Santiago", -33.45f, -70.66f),
    // Fallback location has to be the last entry in the enum, otherwise the random selection of locations could return it
    FALLBACK("Fallback Location, random weather", 0.0f, 0.0f);

    private final String displayName;
    private final float latitude;
    private final float longitude;

    Location(String displayName, float latitude, float longitude) {
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
