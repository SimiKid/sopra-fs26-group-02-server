package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;

public class PlayerGetDTO {
    private WizardClass wizardClass;

    public WizardClass getWizardClass() {
        return wizardClass;
    }

    public void setWizardClass(WizardClass wizardClass) {
        this.wizardClass = wizardClass;
    }
}
