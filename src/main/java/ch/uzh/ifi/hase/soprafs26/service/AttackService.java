package ch.uzh.ifi.hase.soprafs26.service;

//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.UUID;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ResponseStatusException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;

import java.util.List;
import java.util.ArrayList;



@Service
@Transactional
public class AttackService {
    public List<AttackGetDTO> getAllAttacks() {
        List<AttackGetDTO> attackDTOs = new ArrayList<>();

        // Wir nutzen Attack.values(), um alle 8 Attacken aus deinem Code zu holen
    for (Attack attack : Attack.values()) {
        AttackGetDTO dto = new AttackGetDTO();
        dto.setId(attack.getId());
        dto.setName(attack.getDisplayName());
        dto.setBaseDamage(attack.getBaseDamage());
        dto.setElement(attack.getElement());
        dto.setDescription(attack.getDescription());
        attackDTOs.add(dto);
    }
    return attackDTOs;
    }
}