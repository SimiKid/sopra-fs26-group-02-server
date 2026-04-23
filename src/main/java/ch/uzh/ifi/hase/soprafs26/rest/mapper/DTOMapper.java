package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.constant.Attack;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.AttackGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameHistoryEntryDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameSessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PlayerGetDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */

@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE) // ignore unmapped target properties until they are implemented
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "token", target = "token")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "gameCode", target = "gameCode")
	@Mapping(source = "gameStatus", target = "gameStatus")
	@Mapping(source = "player1Id", target = "player1Id")
	@Mapping(source = "player2Id", target = "player2Id")
	@Mapping(source = "activePlayerId", target = "activePlayerId")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(source = "rematchGameCode", target = "rematchGameCode") 
	GameSessionGetDTO convertEntityToGameSessionGetDTO(GameSession gameSession);

	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE) // ignore unmapped target properties until they are implemented
	PlayerGetDTO convertEntityToPlayerGetDTO(Player player);

	@Mapping(source = "displayName", target = "name")
	@Mapping(source = "baseDamage", target = "baseDamage")
	@Mapping(source = "element", target = "element")
	@Mapping(source = "description", target = "description")
	AttackGetDTO convertAttackToAttackGetDTO(Attack attack);

	@Mapping(source = "createdAt", target = "gameDate")
	@Mapping(source = "arenaLocation.displayName", target = "location")
	@Mapping(source = "temperature", target = "temperature")
	@Mapping(source = "rain", target = "rain")
	@BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE) // result + wizard classes are filled in by the service
	GameHistoryEntryDTO convertEntityToGameHistoryEntryDTO(GameSession gameSession);

}