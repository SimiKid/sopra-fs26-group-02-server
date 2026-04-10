package ch.uzh.ifi.hase.soprafs26.rest.dto;
import java.util.List;


public class PlayerGetDTO {
    private Long id;
    private Long userId;
    private String wizardClass;
    private List<String> attacks;
    private Integer hp;
    private Boolean ready;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getWizardClass() {
        return wizardClass;
    }

    public void setWizardClass(String wizardClass) {
        this.wizardClass = wizardClass;
    }

    public List<String> getAttacks() { 
        return attacks; 
    }

    public void setAttacks(List<String> attacks) { 
        this.attacks = attacks; 
    }

    public Integer getHp() {
        return hp;
    }

    public void setHp(Integer hp) {
        this.hp = hp;
    }

    public Boolean getReady() {
        return ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

}
