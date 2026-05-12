package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.EmoteKey;

public class EmoteMessageDTO {
    private EmoteKey emoteKey;

    public void setEmoteKey(EmoteKey emoteKey){
        this.emoteKey = emoteKey;
    }

    public EmoteKey getEmoteKey(){
        return emoteKey;
    }
}
