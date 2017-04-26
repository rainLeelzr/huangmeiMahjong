package com.huangmei.commonhm.model.mahjong.vo;

import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;

import java.util.List;

/**
 * 断线重连，玩家状态正在游戏中时，发送给客户端的对象
 */
public class ReconnectionVo {

    List<RoomMember> roomMembers;

    Room room;

    GameStartVo gameStart;


    /**
     * 剩下可以被摸牌的麻将的数量
     */
    private Integer leftCardCount;

    private List<PersonalCardVo> personalCards;

    public ReconnectionVo(List<RoomMember> roomMembers, Room room, GameStartVo gameStart, Integer leftCardCount) {
        this.roomMembers = roomMembers;
        this.room = room;
        this.gameStart = gameStart;
        this.leftCardCount = leftCardCount;
    }

    public List<PersonalCardVo> getPersonalCards() {
        return personalCards;
    }

    public void setPersonalCards(List<PersonalCardVo> personalCards) {
        this.personalCards = personalCards;
    }

    public List<RoomMember> getRoomMembers() {
        return roomMembers;
    }

    public void setRoomMembers(List<RoomMember> roomMembers) {
        this.roomMembers = roomMembers;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public GameStartVo getGameStart() {
        return gameStart;
    }

    public void setGameStart(GameStartVo gameStart) {
        this.gameStart = gameStart;
    }

    public Integer getLeftCardCount() {
        return leftCardCount;
    }

    public void setLeftCardCount(Integer leftCardCount) {
        this.leftCardCount = leftCardCount;
    }

}
