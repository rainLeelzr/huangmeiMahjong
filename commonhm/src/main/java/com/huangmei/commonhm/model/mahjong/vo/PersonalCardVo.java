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

    /**
     * 打出了的牌，不包括被人碰、杠的牌
     */
    private List<Integer> outCards;

    /**
     * 自己摸到的牌
     *
     * @return
     */
    private Integer touchMahjongId;

    private List<Integer> operatePids;

    public List<Integer> getOperatePids() {
        return operatePids;
    }

    public void setOperatePids(List<Integer> operatePids) {
        this.operatePids = operatePids;
    }

    public Integer getTouchMahjongId() {
        return touchMahjongId;
    }

    public void setTouchMahjongId(Integer touchMahjongId) {
        this.touchMahjongId = touchMahjongId;
    }

    public PersonalCardVo(
            List<Integer> handCardIds,
            List<List<Integer>> pengMahjongIds,
            List<GangVo> gangs,
            List<Integer> outCards,
            Integer touchMahjongId,
            List<Integer> operatePids) {
        this.handCardIds = handCardIds;
        this.pengMahjongIds = pengMahjongIds;
        this.gangs = gangs;
        this.outCards = outCards;
        this.touchMahjongId = touchMahjongId;
        this.operatePids = operatePids;
    }

    public List<Integer> getOutCards() {
        return outCards;
    }

    public void setOutCards(List<Integer> outCards) {
        this.outCards = outCards;
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
