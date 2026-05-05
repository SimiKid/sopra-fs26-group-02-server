package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.LeaderboardGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;


import java.util.List;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaderboardService {
    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<LeaderboardGetDTO> getLeaderboard() {
		List<User> users = userRepository.findTop50ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAsc(0);
		List<LeaderboardGetDTO> leaderboard = new ArrayList<>();
		for (User user : users) {
			leaderboard.add(buildLeaderboard(user));
		}
		return leaderboard;
	}

    public List<LeaderboardGetDTO> getLeaderboard5() {
		List<User> users = userRepository.findTop5ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAsc(0);
		List<LeaderboardGetDTO> leaderboard = new ArrayList<>();
		for (User user : users) {
			leaderboard.add(buildLeaderboard(user));
		}
		return leaderboard;
	}

	private LeaderboardGetDTO buildLeaderboard(User user) {
		LeaderboardGetDTO dto = DTOMapper.INSTANCE.convertEntityToLeaderboardGetDTO(user);
        dto.setUsername(user.getUsername());
        dto.setTotalGames(user.getTotalGames());
        dto.setWins(user.getWins());
        dto.setLosses(user.getLosses());
        dto.setWinRate(user.getWinRate());
		return dto;
	}
}
