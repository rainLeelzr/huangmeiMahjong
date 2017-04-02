package com.huangmei.commonhm.model.mahjong;

import java.util.List;

/**
 * 用于玩家打牌广播
 */
public class GangBroadcast {

    private List<Integer> mahjongIds;// 暗杠的麻将id
    private Integer gangPlayerUId;  // 暗杠玩家uid
    private Integer uId; //需要接受广播消息的玩家uid

    public GangBroadcast() {
    }

    public GangBroadcast(List<Integer> mahjongIds, Integer gangPlayerUId, Integer uId) {
        this.mahjongIds = mahjongIds;
        this.gangPlayerUId = gangPlayerUId;
        this.uId = uId;
    }

    public Integer getGangPlayerUId() {
        return gangPlayerUId;
    }

    public void setGangPlayerUId(Integer gangPlayerUId) {
        this.gangPlayerUId = gangPlayerUId;
    }

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public List<Integer> getMahjongIds() {
        return mahjongIds;
    }

    public void setMahjongIds(List<Integer> mahjongIds) {
        this.mahjongIds = mahjongIds;
    }
}