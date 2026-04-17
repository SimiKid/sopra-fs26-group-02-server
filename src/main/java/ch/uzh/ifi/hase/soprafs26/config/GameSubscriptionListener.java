package ch.uzh.ifi.hase.soprafs26.config;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import ch.uzh.ifi.hase.soprafs26.rest.dto.BattleStateDTO;
import ch.uzh.ifi.hase.soprafs26.service.BattleService;

@Component
public class GameSubscriptionListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final BattleService battleService;

    public GameSubscriptionListener(SimpMessagingTemplate messagingTemplate, BattleService battleService) {
        this.messagingTemplate = messagingTemplate;
        this.battleService = battleService;
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        String dest = SimpMessageHeaderAccessor.wrap(event.getMessage()).getDestination();
        if (dest != null && dest.startsWith("/topic/game/")) {
            String gameCode = dest.substring("/topic/game/".length());
            BattleStateDTO state = battleService.getCurrentState(gameCode);
            if (state != null) {
                messagingTemplate.convertAndSend(dest, state);
            }
        }
    }
}
