package com.huangmei.commonhm.manager.putOutCard;

import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.mahjong.BaseOperate;

import java.util.Set;

/**
 * 储存别人出牌后某个玩家可以的操作
 * 如胡、杠、碰
 */
public class AfterPutOutCardOperate {

    private RoomMember roomMember;

    private Set<BaseOperate> operates;

    public RoomMember getRoomMember() {
        return roomMember;
    }

    public void setRoomMember(RoomMember roomMember) {
        this.roomMember = roomMember;
    }

    public Set<BaseOperate> getOperates() {
        return operates;
    }

    public void setOperates(Set<BaseOperate> operates) {
        this.operates = operates;
    }

    @Override
    public String toString() {
        String o = "";
        for (BaseOperate operate : operates) {
            o += operate.getName() + "、";
        }
        return String.format("{座位%s,可以%s}", roomMember.getSeat(), o);
        //return "{\"AfterPutOutCardOperate\":{"
        //        + "\"roomMember\":" + roomMember
        //        + ", \"operates\":" + operates
        //        + "}}";
    }
}