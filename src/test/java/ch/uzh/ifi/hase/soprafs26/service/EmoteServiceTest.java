package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.EmoteKey;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class EmoteServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    private EmoteService emoteService;

    private User testUser;

    @BeforeEach
    void setup() {
        emoteService = new EmoteService(messagingTemplate, userRepository, authenticationService);

        testUser = new User();
        testUser.setId(1L);
        testUser.setToken("valid-token");

        lenient().when(authenticationService.authenticateByToken("valid-token")).thenReturn(testUser);
    }

    @Test
    void sendEmote_validRequest_broadcastsToCorrectTopic() {
        emoteService.sendEmote("ABC123", "valid-token", EmoteKey.EMOTE_1);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(
            eq("/topic/game/ABC123/emotes"),
            payloadCaptor.capture()
        );
        assertEquals(EmoteKey.EMOTE_1, payloadCaptor.getValue());
    }

    @Test
    void sendEmote_firstEmote_passesRateLimit() {
        // first emote should always go through
        assertDoesNotThrow(() -> emoteService.sendEmote("ABC123", "valid-token", EmoteKey.EMOTE_2));
    }

    @Test
    void sendEmote_twoEmotesWithinRateLimit_throwsTooManyRequests() {
        emoteService.sendEmote("ABC123", "valid-token", EmoteKey.EMOTE_1);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> emoteService.sendEmote("ABC123", "valid-token", EmoteKey.EMOTE_2));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
    }

    @Test
    void sendEmote_twoPlayersSimultaneously_bothPass() {
        User testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setToken("other-token");
        when(authenticationService.authenticateByToken("other-token")).thenReturn(testUser2);

        // two different users emoting at the same time should both pass
        assertDoesNotThrow(() -> emoteService.sendEmote("ABC123", "valid-token", EmoteKey.EMOTE_1));
        assertDoesNotThrow(() -> emoteService.sendEmote("ABC123", "other-token", EmoteKey.EMOTE_1));
    }

    @Test
    void sendEmote_invalidToken_throwsUnauthorized() {
        when(authenticationService.authenticateByToken("bad-token"))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        assertThrows(ResponseStatusException.class,
            () -> emoteService.sendEmote("ABC123", "bad-token", EmoteKey.EMOTE_1));
    }
}