package com.huangmei.commonhm.model.mahjong;

import java.util.List;

/**
 * 用于玩家打牌广播
 */
public class AnGangBroadcast {

    private List<Integer> mahjongIds;// 暗杠的麻将id
    private Integer anGangPlayerUId;  // 暗杠玩家uid
    private Integer uId; //需要接受广播消息的玩家uid

    public AnGangBroadcast() {
    }

    public AnGangBroadcast(List<Integer> mahjongIds, Integer anGangPlayerUId, Integer uId) {
        this.mahjongIds = mahjongIds;
        this.anGangPlayerUId = anGangPlayerUId;
        this.uId = uId;
    }

    public Integer getAnGangPlayerUId() {
        return anGangPlayerUId;
    }

    public void setAnGangPlayerUId(Integer anGangPlayerUId) {
        this.anGangPlayerUId = anGangPlayerUId;
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