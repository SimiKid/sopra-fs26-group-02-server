package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeaderboardServiceTest {

    private UserRepository userRepository;
    private LeaderboardService leaderboardService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        leaderboardService = new LeaderboardService(userRepository);
    }

    @Test
    void getLeaderboard_returnsCorrectLeaderboardList_whenUsersExist() {
        // given
        User user1 = new User();
        user1.setUsername("alice");
        user1.setWins(12);
        user1.setTotalGames(18);

        User user2 = new User();
        user2.setUsername("bob");
        user2.setWins(10);
        user2.setTotalGames(14);

        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(userRepository.findTop50ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0))
                .thenReturn(expectedUsers);

        // when
        List<User> leaderboard = leaderboardService.getLeaderboard();

        // then
        assertNotNull(leaderboard);
        assertEquals(2, leaderboard.size());
        assertEquals("alice", leaderboard.get(0).getUsername());
        assertEquals(12, leaderboard.get(0).getWins());
        assertEquals("bob", leaderboard.get(1).getUsername());
        assertEquals(10, leaderboard.get(1).getWins());

        verify(userRepository, times(1)).findTop50ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0);
    }

    @Test
    void getLeaderboard_returnsEmptyList_whenNoUsersExist() {
        // given
        when(userRepository.findTop50ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0))
                .thenReturn(Collections.emptyList());

        // when
        List<User> leaderboard = leaderboardService.getLeaderboard();

        // then
        assertNotNull(leaderboard);
        assertTrue(leaderboard.isEmpty());

        verify(userRepository, times(1)).findTop50ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0);
    }

    @Test
    void getLeaderboard5_returnsTop5Users_whenUsersExist() {
        // given
        User user1 = new User(); user1.setUsername("a"); user1.setWins(10); user1.setTotalGames(20);
        User user2 = new User(); user2.setUsername("b"); user2.setWins(9); user2.setTotalGames(18);
        User user3 = new User(); user3.setUsername("c"); user3.setWins(8); user3.setTotalGames(15);
        User user4 = new User(); user4.setUsername("d"); user4.setWins(7); user4.setTotalGames(16);
        User user5 = new User(); user5.setUsername("e"); user5.setWins(6); user5.setTotalGames(17);

        List<User> expectedUsers = Arrays.asList(user1, user2, user3, user4, user5);
        when(userRepository.findTop5ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0))
                .thenReturn(expectedUsers);

        // when
        List<User> leaderboardTop5 = leaderboardService.getLeaderboard5();

        // then
        assertNotNull(leaderboardTop5);
        assertEquals(5, leaderboardTop5.size());
        assertEquals("a", leaderboardTop5.get(0).getUsername());
        assertEquals("e", leaderboardTop5.get(4).getUsername());
        verify(userRepository, times(1)).findTop5ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0);
    }

    @Test
    void getLeaderboard5_returnsEmptyList_whenNoUsersExist() {
        // given
        when(userRepository.findTop5ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0))
                .thenReturn(Collections.emptyList());

        // when
        List<User> leaderboardTop5 = leaderboardService.getLeaderboard5();

        // then
        assertNotNull(leaderboardTop5);
        assertTrue(leaderboardTop5.isEmpty());
        verify(userRepository, times(1)).findTop5ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0);
    }

}