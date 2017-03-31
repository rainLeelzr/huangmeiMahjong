package com.huangmei.commonhm.model.mahjong;

/**
 * 用于玩家打牌广播
 */
public class PlayedMahjong {

    private Integer leftCardCount; //剩下可以摸牌的数量
    private Integer playedMahjongId;// 打出的麻将id
    private Integer playedUId;  // 打牌的玩家uid
    private Integer uId; //需要接受广播消息的玩家uid
    private Long version;

    public Integer getLeftCardCount() {
        return leftCardCount;
    }

    public void setLeftCardCount(Integer leftCardCount) {
        this.leftCardCount = leftCardCount;
    }

    public Integer getPlayedMahjongId() {
        return playedMahjongId;
    }

    public void setPlayedMahjongId(Integer playedMahjongId) {
        this.playedMahjongId = playedMahjongId;
    }

    public Integer getPlayedUId() {
        return playedUId;
    }

    public void setPlayedUId(Integer playedUId) {
        this.playedUId = playedUId;
    }

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}