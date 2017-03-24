package com.huangmei.commonhm.model.mahjong;

import com.huangmei.commonhm.model.RoomMember;

import java.util.Set;

/**
 * 单个玩家拥有的牌信息
 */
public class PersonalCardInfo {

    /**
     * 此副手牌对应的roomMember对象
     */
    private RoomMember roomMember;

    /**
     * 手牌
     */
    private Set<Mahjong> handCards;

    /**
     * 在剩下的牌中，摸一张牌
     */
    private Mahjong touchMahjong;

    public Mahjong getTouchMahjong() {
        return touchMahjong;
    }

    public void setTouchMahjong(Mahjong touchMahjong) {
        this.touchMahjong = touchMahjong;
    }

    @Override
    public String toString() {
        return "{\"PersonalCardInfo\":{"
                + "\"roomMember\":" + roomMember
                + ", \"handCards\":" + handCards
                + ", \"touchMahjong\":" + touchMahjong + ""
                + "}}";
    }

    public RoomMember getRoomMember() {
        return roomMember;
    }

    public void setRoomMember(RoomMember roomMember) {
        this.roomMember = roomMember;
    }

    public Set<Mahjong> getHandCards() {
        return handCards;
    }

    public void setHandCards(Set<Mahjong> handCards) {
        this.handCards = handCards;
    }
}
