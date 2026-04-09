package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;

public class PlayerGetDTO {
    private int hp;
    private WizardClass wizardClass;

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public WizardClass getWizardClass() {
        return wizardClass;
    }

    public void setWizardClass(WizardClass wizardClass) {
        this.wizardClass = wizardClass;
    }


}
