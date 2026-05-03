package ch.uzh.ifi.hase.soprafs26.entity;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import ch.uzh.ifi.hase.soprafs26.constant.WizardClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "players")
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long gameSessionId;

    @Enumerated(EnumType.STRING)
    private WizardClass wizardClass;

    private String attack1;
    private String attack2;
    private String attack3;

    public List<String> getAttacks() {
        List<String> attacks = new ArrayList<>();
        if (this.attack1 != null) attacks.add(this.attack1);
        if (this.attack2 != null) attacks.add(this.attack2);
        if (this.attack3 != null) attacks.add(this.attack3);
        return attacks;
    }

    @Column(nullable = false)
    private int hp;

    @Column(nullable = false)
    private int maxHp;

    @Column(nullable = false)
    private boolean ready;

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

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public WizardClass getWizardClass() {
        return wizardClass;
    }   

    public void setWizardClass(WizardClass wizardClass) {
        this.wizardClass = wizardClass;
    }

    public String getAttack1() {
        return attack1;
    }

    public void setAttack1(String attack1) {
        this.attack1 = attack1;
    }

    public String getAttack2() {
        return attack2;
    }

    public void setAttack2(String attack2) {
        this.attack2 = attack2;
    }

    public String getAttack3() {
        return attack3;
    }

    public void setAttack3(String attack3) {
        this.attack3 = attack3;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    
    
}
