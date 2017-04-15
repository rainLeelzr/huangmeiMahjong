package com.huangmei.commonhm.model.mahjong.vo;


import java.util.List;

/**
 * 游戏开始后广播
 */
public class GameStartVo {

    /**
     * 庄家的uId，先设置为userId，在api层转换为uId
     */

    private Integer bankerUId;

    /**
     * 骰子
     */
    private Integer[] dices;

    /**
     * 宝娘
     */
    private Integer baoMotherId;
    /**
     * 宝牌
     */
    private List<Integer> baoMahjongs;
    /**
     * 当前局数
     */
    private Integer currentTimes;

    public Integer getCurrentTimes() {
        return currentTimes;
    }

    public void setCurrentTimes(Integer currentTimes) {
        this.currentTimes = currentTimes;
    }

    public Integer getBankerUId() {
        return bankerUId;
    }

    public void setBankerUId(Integer bankerUId) {
        this.bankerUId = bankerUId;
    }

    public Integer[] getDices() {
        return dices;
    }

    public void setDices(Integer[] dices) {
        this.dices = dices;
    }

    public Integer getBaoMotherId() {
        return baoMotherId;
    }

    public void setBaoMotherId(Integer baoMotherId) {
        this.baoMotherId = baoMotherId;
    }

    public List<Integer> getBaoMahjongs() {
        return baoMahjongs;
    }

    public void setBaoMahjongs(List<Integer> baoMahjongs) {
        this.baoMahjongs = baoMahjongs;
    }
}
