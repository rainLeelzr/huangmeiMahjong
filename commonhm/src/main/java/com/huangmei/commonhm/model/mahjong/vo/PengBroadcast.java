package com.huangmei.commonhm.model.mahjong.vo;

import java.util.List;

/**
 * 用于玩家碰广播
 */
public class PengBroadcast {

    private List<Integer> mahjongIds;// 碰的麻将id
    private List<Integer> playedUId;// 出牌的玩家uid
    private List<Integer> playedMahjongId;// 别人打出的麻将
    private Integer pengPlayerUId;  // 碰玩家uid
    private Integer uId; //需要接受广播消息的玩家uid

    /**
     * 个人的手牌id
     */
    private List<Integer> handCardIds;

    /**
     * 碰了的牌
     */
    private List<List<Integer>> pengMahjongIds;

    /**
     * 杠了的牌
     */
    private List<GangVo> gangs;

    /**
     * 碰的类型，与pid相对应
     */
    private Integer operatePid;

    public PengBroadcast() {
    }

    public PengBroadcast(
            Integer uId,
            //List<Integer> playedUId,
            //List<Integer> playedMahjongId,
            List<Integer> mahjongIds,
            Integer pengPlayerUId,
            Integer operatePid,
            List<Integer> handCardIds,
            List<List<Integer>> pengMahjongIds,
            List<GangVo> gangs) {
        this.uId = uId;
        //this.playedUId = playedUId;
        //this.playedMahjongId = playedMahjongId;
        this.mahjongIds = mahjongIds;
        this.pengPlayerUId = pengPlayerUId;
        this.operatePid = operatePid;
        this.handCardIds = handCardIds;
        this.pengMahjongIds = pengMahjongIds;
        this.gangs = gangs;
    }

    public List<Integer> getPlayedUId() {
        return playedUId;
    }

    public void setPlayedUId(List<Integer> playedUId) {
        this.playedUId = playedUId;
    }

    public List<Integer> getPlayedMahjongId() {
        return playedMahjongId;
    }

    public void setPlayedMahjongId(List<Integer> playedMahjongId) {
        this.playedMahjongId = playedMahjongId;
    }

    public List<Integer> getHandCardIds() {
        return handCardIds;
    }

    public void setHandCardIds(List<Integer> handCardIds) {
        this.handCardIds = handCardIds;
    }

    public List<List<Integer>> getPengMahjongIds() {
        return pengMahjongIds;
    }

    public void setPengMahjongIds(List<List<Integer>> pengMahjongIds) {
        this.pengMahjongIds = pengMahjongIds;
    }

    public List<GangVo> getGangs() {
        return gangs;
    }

    public void setGangs(List<GangVo> gangs) {
        this.gangs = gangs;
    }

    public Integer getOperatePid() {
        return operatePid;
    }

    public void setOperatePid(Integer operatePid) {
        this.operatePid = operatePid;
    }

    public Integer getPengPlayerUId() {
        return pengPlayerUId;
    }

    public void setPengPlayerUId(Integer pengPlayerUId) {
        this.pengPlayerUId = pengPlayerUId;
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