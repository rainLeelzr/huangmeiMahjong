package com.huangmei.commonhm.model.mahjong;


import java.util.List;

/**
 * 发一张牌给客户端
 */
public class ClientTouchMahjong {

    // 需要接收本对象的玩家uId
    private Integer uId;

    /**
     * 剩下可以被摸牌的麻将的数量
     */
    private Integer leftCardCount;

    /**
     * 摸牌后可以的操作，如胡、杠、碰，不包含过
     */
    private List<Integer> operates;

    /**
     * 摸到的麻将
     */
    private Integer touchMahjong;

    /**
     * 消息版本号
     */
    private Long version;

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public Integer getLeftCardCount() {
        return leftCardCount;
    }

    public void setLeftCardCount(Integer leftCardCount) {
        this.leftCardCount = leftCardCount;
    }

    public List<Integer> getOperates() {
        return operates;
    }

    public void setOperates(List<Integer> operates) {
        this.operates = operates;
    }

    public Integer getTouchMahjong() {
        return touchMahjong;
    }

    public void setTouchMahjong(Integer touchMahjong) {
        this.touchMahjong = touchMahjong;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
