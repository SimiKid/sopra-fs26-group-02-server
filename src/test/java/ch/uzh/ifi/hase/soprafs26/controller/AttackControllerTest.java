package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.AttackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttackController.class)
public class AttackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttackService attackService;

    private static AttackGetDTO toDto(Attack attack) {
        AttackGetDTO dto = new AttackGetDTO();
        dto.setId(attack.getId());
        dto.setName(attack.getDisplayName());
        dto.setBaseDamage(attack.getBaseDamage());
        dto.setElement(attack.getElement());
        dto.setDescription(attack.getDescription());
        return dto;
    }

    @Test
    void getAttacks_returnsOkAndBodyFromService() throws Exception {
        List<AttackGetDTO> attacks = Stream.of(Attack.values())
            .map(AttackControllerTest::toDto)
            .collect(Collectors.toList());
        given(attackService.getAllAttacks()).willReturn(attacks);

        Attack sample = Attack.values()[0];

        mockMvc.perform(get("/attacks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", is(Attack.values().length)))
            .andExpect(jsonPath("$[0].id", is(sample.getId())))
            .andExpect(jsonPath("$[0].name", is(sample.getDisplayName())))
            .andExpect(jsonPath("$[0].baseDamage", is(sample.getBaseDamage())))
            .andExpect(jsonPath("$[0].element", is(sample.getElement().name())))
            .andExpect(jsonPath("$[0].description", is(sample.getDescription())));

        verify(attackService).getAllAttacks();
    }
}
