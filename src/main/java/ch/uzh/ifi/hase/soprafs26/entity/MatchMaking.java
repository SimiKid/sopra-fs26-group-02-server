package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import jakarta.persistence.MapsId;
import java.time.LocalDateTime;
import java.io.Serializable;

@Entity
@Table(name = "match_making")
public class MatchMaking implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @OneToOne
    @MapsId
    private User user; 

    private LocalDateTime joinedAt; 

    private String matchedGameCode; 
    


public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public User getUser() {
    return user;
}   

public void setUser(User user) {
    this.user = user;
}

public LocalDateTime getJoinedAt() {
    return joinedAt;
}

public void setJoinedAt(LocalDateTime joinedAt) {
    this.joinedAt = joinedAt;
}

public String getMatchedGameCode() {
    return matchedGameCode;
}

public void setMatchedGameCode(String matchedGameCode) {
    this.matchedGameCode = matchedGameCode;
}
}