package ch.uzh.ifi.hase.soprafs26.constant;

public enum WizardClass {
    ATTACKWIZARD(0.8, 1.5),
    TANKWIZARD(1.5, 0.8),
    BALANCEDWIZARD(1.0, 1.0),
    GAMBLERWIZARD(1.0, 1.0); // base value, actual value will be determined randomly at the start of each game / round
    
    final double hpMultiplier;
    final double attackMultiplier;

    private WizardClass(double hp, double attackMultiplier) {
        this.hpMultiplier = hp;
        this.attackMultiplier = attackMultiplier;
    }

    public double getAttackMultiplier() {
        if (this == GAMBLERWIZARD) {
            // Randomly determine the attack modifier for the gambler wizard at the start of each round
            return Math.random() + 0.5;  // 0.5–1.5
        }
        return attackMultiplier;
    }
}
