package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import ch.uzh.ifi.hase.soprafs26.constant.EmoteKey;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EmoteMessageDTO;
import ch.uzh.ifi.hase.soprafs26.service.BattleService;
import ch.uzh.ifi.hase.soprafs26.service.EmoteService;

/**
 * STOMP message handler for in-battle actions. Clients send an attack to
 * /app/game/{gameCode}/attack; BattleService resolves it and broadcasts
 * the resulting state to /topic/game/{gameCode}.
 */
@Controller
public class BattleWebSocketController {

    private final BattleService battleService;
    private final EmoteService emoteService;

    public BattleWebSocketController(BattleService battleService, EmoteService emoteService) {
        this.battleService = battleService;
        this.emoteService = emoteService;
    }

    @MessageMapping("/game/{gameCode}/attack")
    public void handleAttack(
            @DestinationVariable ("gameCode") String gameCode,
            @Header("Authorization") String token,
            @Payload String attackName) {
        
        battleService.resolveAttack(gameCode, token, attackName);
    }

    @MessageMapping("/game/{gameCode}/emote")
    public void sendEmote(
        @DestinationVariable ("gameCode") String gameCode,
        @Header("Authorization") String token,
        @Payload EmoteMessageDTO emoteMessage
    ) {
        emoteService.sendEmote(gameCode, token, emoteMessage.getEmoteKey());
    }
}