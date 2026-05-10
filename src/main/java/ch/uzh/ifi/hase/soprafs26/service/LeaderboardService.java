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

    public List<User> getLeaderboard() {
		List<User> users = userRepository.findTop50ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0);
		List<User> leaderboard = new ArrayList<>();
		for (User user : users) {
			leaderboard.add(user);
		}
		return leaderboard;
	}

    public List<User> getLeaderboard5() {
		List<User> users = userRepository.findTop5ByTotalGamesGreaterThanOrderByWinsDescTotalGamesAscUsernameAsc(0);
		List<User> leaderboard = new ArrayList<>();
		for (User user : users) {
			leaderboard.add(user);
		}
		return leaderboard;
	}

}
