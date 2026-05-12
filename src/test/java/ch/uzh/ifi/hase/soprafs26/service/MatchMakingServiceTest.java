package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.MatchMaking;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MatchMakingRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MatchMakingServiceTest {

    @Mock
    private MatchMakingRepository matchMakingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private SimpMessagingTemplate simpleMessagingTemplate;

    @Mock
    private GameSessionService gameSessionService;

    @InjectMocks
    private MatchMakingService matchMakingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        matchMakingService = new MatchMakingService(matchMakingRepository, userRepository, gameSessionRepository, simpleMessagingTemplate, gameSessionService);
    }

    @Test
    void joinRandomGameSession_CreatesAndJoins_WhenNoOpponent() {
        // given
        Long userId = 1L;
        MatchMaking entry = new MatchMaking();
        entry.setId(userId);

        when(matchMakingRepository.findById(userId)).thenReturn(Optional.empty());
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matchMakingRepository.save(any(MatchMaking.class))).thenReturn(entry);
        when(matchMakingRepository.findFirstByIdNotAndMatchedGameCodeIsNullOrderByJoinedAtAsc(userId)).thenReturn(Optional.empty());

        // when
        matchMakingService.joinRandomGameSession(userId);

        // then
        verify(matchMakingRepository, times(1)).save(any(MatchMaking.class));
        verify(matchMakingRepository, never()).delete(any());
        verify(gameSessionService, never()).createGameSession(any());
        verify(simpleMessagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void joinRandomGameSession_PairsWithOpponent_AndDeletesEntriesAndSendsNotification() {
        // given
        Long userId = 1L;
        Long opponentId = 2L;
        MatchMaking myEntry = new MatchMaking();
        myEntry.setId(userId);

        MatchMaking opponentEntry = new MatchMaking();
        opponentEntry.setId(opponentId);

        when(matchMakingRepository.findById(userId)).thenReturn(Optional.empty());
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(matchMakingRepository.save(any(MatchMaking.class))).thenReturn(myEntry);

        when(matchMakingRepository.findFirstByIdNotAndMatchedGameCodeIsNullOrderByJoinedAtAsc(userId))
                .thenReturn(Optional.of(opponentEntry));

        GameSession gameSession = new GameSession();
        gameSession.setId(100L);
        gameSession.setGameCode("XYZ123");

        when(gameSessionService.createGameSession(any())).thenReturn(gameSession);

        // when
        matchMakingService.joinRandomGameSession(userId);

        // then
        verify(matchMakingRepository, atLeastOnce()).save(any(MatchMaking.class));
        verify(matchMakingRepository, times(1)).delete(myEntry);
        verify(matchMakingRepository, times(1)).delete(opponentEntry);
        verify(simpleMessagingTemplate, times(1)).convertAndSend("/topic/match/" + userId, gameSession.getGameCode());
        verify(simpleMessagingTemplate, times(1)).convertAndSend("/topic/match/" + opponentId, gameSession.getGameCode());
        assertThat(myEntry.getMatchedGameCode()).isEqualTo(gameSession.getGameCode());
        assertThat(opponentEntry.getMatchedGameCode()).isEqualTo(gameSession.getGameCode());
    }

    @Test
    void leaveGameSession_RemovesEntry_IfExists() {
        // given
        Long userId = 10L;
        MatchMaking entry = new MatchMaking();
        entry.setId(userId);

        when(matchMakingRepository.findById(userId)).thenReturn(Optional.of(entry));

        // when
        matchMakingService.leaveGameSession(userId);

        // then
        verify(matchMakingRepository).delete(entry);
    }

    @Test
    void leaveGameSession_ThrowsException_IfUserNotFound() {
        // given
        Long userId = 42L;
        when(matchMakingRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> matchMakingService.leaveGameSession(userId));

        // then
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        assertThat(((ResponseStatusException)thrown).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void joinRandomGameSession_UpdatesJoinedAt_IfAlreadyJoined() {
        // given
        Long userId = 555L;
        MatchMaking entry = new MatchMaking();
        entry.setId(userId);
        entry.setJoinedAt(LocalDateTime.now().minusHours(1));
        when(matchMakingRepository.findById(userId)).thenReturn(Optional.of(entry));
        when(matchMakingRepository.save(any(MatchMaking.class))).thenReturn(entry);
        when(matchMakingRepository.findFirstByIdNotAndMatchedGameCodeIsNullOrderByJoinedAtAsc(userId)).thenReturn(Optional.empty());

        // when
        matchMakingService.joinRandomGameSession(userId);

        // then
        verify(matchMakingRepository, times(1)).save(entry);
    }

    @Test
    void purgeExpiredEntries_DeletesOldEntries() {
        // given
        // No setup necessary, just check correct repository call.

        // when
        matchMakingService.purgeExpiredEntries();

        // then
        verify(matchMakingRepository, times(1)).deleteByJoinedAtBeforeAndMatchedGameCodeIsNull(any(LocalDateTime.class));
    }
}

