package com.huangmei.commonhm.model.mahjong.vo;

import java.util.List;

public class PersonalCardVo {
    private Integer uId;

    //个人的手牌id
    private List<Integer> handCardIds;

    /**
     * 碰了的牌
     */
    private List<List<Integer>> pengMahjongIds;

    /**
     * 杠了的牌
     */
    private List<GangVo> gangs;

    public PersonalCardVo(List<Integer> handCardIds, List<List<Integer>> pengMahjongIds, List<GangVo> gangs) {
        this.handCardIds = handCardIds;
        this.pengMahjongIds = pengMahjongIds;
        this.gangs = gangs;
    }

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
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
}
