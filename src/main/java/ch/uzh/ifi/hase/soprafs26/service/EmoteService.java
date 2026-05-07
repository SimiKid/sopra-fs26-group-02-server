package ch.uzh.ifi.hase.soprafs26.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.EmoteKey;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
public class EmoteService {
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthenticationService authenticationService;
    private final ConcurrentHashMap<Long, Long> lastEmoteTime = new ConcurrentHashMap<>();

    public EmoteService(SimpMessagingTemplate messagingTemplate, UserRepository userRepository, AuthenticationService authenticationService){
        this.messagingTemplate = messagingTemplate;
        this.authenticationService = authenticationService;
    }

    public void sendEmote(String gameCode, String token, EmoteKey emoteKey) {
        User user = authenticationService.authenticateByToken(token);

        long now = System.currentTimeMillis();
        long lastTime = lastEmoteTime.getOrDefault(user.getId(), 0L);

        if (now - lastTime < 3000) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Emote rate limit exceeded");
        }

        lastEmoteTime.put(user.getId(), now);

        messagingTemplate.convertAndSend("/topic/game/" + gameCode + "/emotes", emoteKey);
    }
}