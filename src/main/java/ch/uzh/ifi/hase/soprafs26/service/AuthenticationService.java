package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
@Transactional
public class AuthenticationService {

        private final UserRepository userRepository;

        public AuthenticationService(@Qualifier("userRepository") UserRepository userRepository) {
            this.userRepository = userRepository;
        }

    	public User loginUser(User userToBeLoggedIn) {
		    User userByUsername = userRepository.findByUsername(userToBeLoggedIn.getUsername());
            if (userByUsername == null || !userByUsername.getPassword().equals(userToBeLoggedIn.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }
            userByUsername.setStatus(UserStatus.ONLINE);
            userByUsername.setToken(UUID.randomUUID().toString());
            userRepository.save(userByUsername);
            return userByUsername;
	}
    
}
