package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.LeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito; 

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaderboardControllerTest {

    private LeaderboardService leaderboardService;
    private LeaderboardController leaderboardController;

    @BeforeEach
    void setup() {
        leaderboardService = Mockito.mock(LeaderboardService.class);
        leaderboardController = new LeaderboardController(leaderboardService);
    }

    @Test
    void getLeaderboard_returnsLeaderboardList_whenUsersExist() {
        // given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setWins(10);
        user1.setTotalGames(12);
        User user2 = new User();
        user2.setUsername("user2");
        user2.setWins(8);
        user2.setTotalGames(10);
        List<User> users = Arrays.asList(user1, user2);
        when(leaderboardService.getLeaderboard()).thenReturn(users);

        // when
        List<LeaderboardGetDTO> leaderboard = leaderboardController.getLeaderboard();

        // then
        assertNotNull(leaderboard);
        assertEquals(2, leaderboard.size());
        assertEquals("user1", leaderboard.get(0).getUsername());
        assertEquals("user2", leaderboard.get(1).getUsername());
        assertEquals(10, leaderboard.get(0).getWins());
        assertEquals(8, leaderboard.get(1).getWins());
        // Check that the top user is user1 with highest wins
        assertTrue(leaderboard.get(0).getWins() >= leaderboard.get(1).getWins());
        verify(leaderboardService, times(1)).getLeaderboard();
    }


    @Test
    void getLeaderboard5_returnsTop5Users_whenUsersExist() {
        // given
        User user1 = new User();
        user1.setUsername("user1");
        user1.setWins(10);
        user1.setTotalGames(12);
        User user2 = new User();
        user2.setUsername("user2");
        user2.setWins(9);
        user2.setTotalGames(11);
        User user3 = new User();
        user3.setUsername("user3");
        user3.setWins(7);
        user3.setTotalGames(9);
        User user4 = new User();
        user4.setUsername("user4");
        user4.setWins(6);
        user4.setTotalGames(11);
        User user5 = new User();
        user5.setUsername("user5");
        user5.setWins(5);
        user5.setTotalGames(13);

        List<User> users = Arrays.asList(user1, user2, user3, user4, user5);
        when(leaderboardService.getLeaderboard5()).thenReturn(users);

        // when
        List<LeaderboardGetDTO> leaderboardTop5 = leaderboardController.getLeaderboard5();

        // then
        assertNotNull(leaderboardTop5);
        assertEquals(5, leaderboardTop5.size());
        assertEquals("user1", leaderboardTop5.get(0).getUsername());
        assertEquals("user5", leaderboardTop5.get(4).getUsername());
        verify(leaderboardService, times(1)).getLeaderboard5();
    }

    @Test
    void getLeaderboard_returnsEmptyList_whenNoUsersExist() {
        // given
        when(leaderboardService.getLeaderboard()).thenReturn(Arrays.asList());

        // when
        List<LeaderboardGetDTO> leaderboard = leaderboardController.getLeaderboard();

        // then
        assertNotNull(leaderboard);
        assertTrue(leaderboard.isEmpty());
        verify(leaderboardService, times(1)).getLeaderboard();
    }

    @Test
    void getLeaderboard5_returnsEmptyList_whenNoUsersExist() {
        // given
        when(leaderboardService.getLeaderboard5()).thenReturn(Arrays.asList());

        // when
        List<LeaderboardGetDTO> leaderboardTop5 = leaderboardController.getLeaderboard5();

        // then
        assertNotNull(leaderboardTop5);
        assertTrue(leaderboardTop5.isEmpty());
        verify(leaderboardService, times(1)).getLeaderboard5();
    }
}

