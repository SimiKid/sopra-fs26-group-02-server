package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.Element;

public class AttackGetDTO {
    private String id;
    private String name;
    private int baseDamage;
    private Element element;
    private String description;

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public int getBaseDamage() {
        return baseDamage;
    }
    
    public void setBaseDamage(int baseDamage) { 
        this.baseDamage = baseDamage;
    }

    public Element getElement() {
        return element;
    }
    
    public void setElement(Element element) {
        this.element = element;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}