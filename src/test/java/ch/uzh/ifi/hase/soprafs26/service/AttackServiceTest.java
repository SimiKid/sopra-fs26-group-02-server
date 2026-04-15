package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.GameStatus;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlayerRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class AttackServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private GameSessionRepository gameSessionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AttackService attackService;

    private GameSession session;
    private Player player1;
    private Player player2;
    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        session = new GameSession();
        session.setGameCode("ABC123");
        session.setGameStatus(GameStatus.CONFIGURING);
        session.setPlayer1Id(1L);
        session.setPlayer2Id(2L);

        player1 = new Player();
        player1.setUserId(1L);
        player1.setReady(true); // already ready from before

        player2 = new Player();
        player2.setUserId(2L);
        player2.setReady(false); // this is the player submitting now

        user = new User();
        user.setId(2L);
        user.setToken("token-p2");
        user.setCurrentGameSessionId(1L);
        session.setId(1L);
    }

    @Test
    void setAttacks_bothPlayersReady_setsGameStatusToBattleAndAssignsActivePlayer() {
        // given - player2 is now submitting their attacks, making both ready
        given(authenticationService.authenticateByToken("token-p2")).willReturn(user);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);
        given(playerRepository.findByUserId(2L)).willReturn(player2);
        given(playerRepository.findByUserId(1L)).willReturn(player1);
        given(playerRepository.save(any(Player.class))).willAnswer(i -> i.getArgument(0));
        given(gameSessionRepository.save(any(GameSession.class))).willAnswer(i -> i.getArgument(0));

        // when
        attackService.setAttacks("ABC123", List.of("FIREBALL", "LIGHTNING", "TORNADO"), "token-p2");

        // then
        assertEquals(GameStatus.BATTLE, session.getGameStatus());
        assertTrue(
            session.getActivePlayerId().equals(1L) || session.getActivePlayerId().equals(2L),
            "activePlayerId must be either player1 or player2"
        );
        verify(gameSessionRepository).save(session);
    }

    @Test
    void setAttacks_onlyOnePlayerReady_doesNotChangeToBattle() {
        // given - player1 is NOT ready yet
        player1.setReady(false);
        given(authenticationService.authenticateByToken("token-p2")).willReturn(user);
        given(gameSessionRepository.findByGameCode("ABC123")).willReturn(session);
        given(playerRepository.findByUserId(2L)).willReturn(player2);
        given(playerRepository.findByUserId(1L)).willReturn(player1);
        given(playerRepository.save(any(Player.class))).willAnswer(i -> i.getArgument(0));

        // when
        attackService.setAttacks("ABC123", List.of("FIREBALL", "LIGHTNING", "TORNADO"), "token-p2");

        // then
        assertEquals(GameStatus.CONFIGURING, session.getGameStatus());
        assertNull(session.getActivePlayerId());
    }
}