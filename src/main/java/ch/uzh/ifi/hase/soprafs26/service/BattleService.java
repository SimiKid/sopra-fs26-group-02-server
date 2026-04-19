package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs26.repository.BattleRepository;
import ch.uzh.ifi.hase.soprafs26.entity.Battle;
import ch.uzh.ifi.hase.soprafs26.constant.Element;
import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.constant.WeatherModifier;
import ch.uzh.ifi.hase.soprafs26.constant.TemperatureCategory;
import ch.uzh.ifi.hase.soprafs26.constant.RainCategory;
import java.util.Map;
import java.util.regex.Pattern;
import ch.uzh.ifi.hase.soprafs26.entity.Player;

@Service
@Transactional
public class BattleService {
    
    private final Logger log = LoggerFactory.getLogger(BattleService.class);
    private final BattleRepository battleRepository;
    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;
    public BattleService(BattleRepository battleRepository, GameSessionRepository gameSessionRepository, PlayerRepository playerRepository) {
        this.battleRepository = battleRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.playerRepository = playerRepository;
    }

    public int[] turnBattle(String gameCode, String attack) {
        //find attack and element
        String cleanAttackName = attack.replace("\"", "").trim().toUpperCase();
        Attack attackEnum = Attack.valueOf(cleanAttackName);
        Element element = attackEnum.getElement();
        //get temprature and rain
        GameSession gameSession = gameSessionRepository.findByGameCode(gameCode);
        TemperatureCategory temperature = gameSession.getTemperature();
        RainCategory rain = gameSession.getRain();
        //get weather modifier
        Map<Object, Double> elementMatrix = WeatherModifier.ELEMENT_MODIFIERS.get(element);
        double tempFactor = elementMatrix.getOrDefault(temperature, 1.0);
        double rainFactor = elementMatrix.getOrDefault(rain, 1.0);
        double weatherModifier = tempFactor * rainFactor;
        //calculate damage
        int damage = (int) (attackEnum.getBaseDamage() * weatherModifier);
        //update hp
        Player player = playerRepository.findById(gameSession.getActivePlayerId())
            .orElseThrow(() -> new RuntimeException("Player not found"));
        player.setHp(player.getHp() - damage);
        playerRepository.save(player);
        return new int[] {damage, player.getHp()};
}
}