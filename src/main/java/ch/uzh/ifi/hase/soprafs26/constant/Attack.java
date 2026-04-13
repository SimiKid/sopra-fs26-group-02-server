package ch.uzh.ifi.hase.soprafs26.constant;

/**
 * Fixed attack pool. Each constant holds static metadata for the REST API.
 * Adjust names, numbers, elements, and descriptions as you like.
 */
public enum Attack {

    FIREBALL("Fireball", 30, Element.FIRE, "A classic explosive burst of flame."),
    INFERNO("Inferno", 30, Element.FIRE, "Overwhelming heat that scorches a wide area."),
    LIGHTING("Lightning", 30, Element.LIGHTNING, "A fast bolt that shocks the target."),
    TORNADO("Tornado", 30, Element.NEUTRAL, "Swirling winds that throw the enemy off balance."),
    BLIZZARD("Blizzard", 30, Element.ICE, "Freezing wind and hail that chip away defenses."),
    PUNCH("Punch", 30, Element.NEUTRAL, "A simple but reliable melee strike."),
    ICESPIKES("Ice Spikes", 30, Element.ICE, "Sharp shards erupt from the ground."),
    TSUNAMI("Tsunami", 30, Element.LIGHTNING, "A crushing wave that floods the battlefield.");

    private final String displayName;
    private final int baseDamage;
    private final Element element;
    private final String description;

    Attack(String displayName, int baseDamage, Element element, String description) {
        this.displayName = displayName;
        this.baseDamage = baseDamage;
        this.element = element;
        this.description = description;
    }

    /** Stable id for APIs and persistence (e.g. stored in Player.attack1–3). */
    public String getId() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public Element getElement() {
        return element;
    }

    public String getDescription() {
        return description;
    }
}
