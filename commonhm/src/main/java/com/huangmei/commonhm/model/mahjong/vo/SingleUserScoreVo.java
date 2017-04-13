package com.huangmei.commonhm.model.mahjong.vo;

import com.huangmei.commonhm.model.Score;
import com.huangmei.commonhm.util.CommonError;

import java.util.List;

/**
 * 单局结算界面，每个玩家的信息
 */
public class SingleUserScoreVo {


    private String nickName;
    private String image;

    /**
     * [12,22]一个数组存牌的ID 12 是二万 22是二筒
     */
    private List<Integer> peng;
    private List<Integer> gang;
    private List<Integer> card;

    /**
     * 炮数
     */
    private Integer paoShu;

    /**
     * 分数
     */
    private Integer score;

    /**
     * 1 自摸 2 接炮 3 输
     */
    private Integer state;

    /**
     * 点炮,接炮和自摸的牌  0表示该玩家没有这个内容
     */
    private Integer otherCard;

    /**
     * score对象的winType
     */
    public static Integer parseToState(Integer winType) {
        if (Score.WinType.ZI_MO.getId().equals(winType)) {
            return State.ZI_MO.getId();
        } else if (Score.WinType.JIE_PAO.getId().equals(winType)) {
            return State.JIE_PAO.getId();
        } else if (Score.WinType.DIAN_PAO.getId().equals(winType)
                || Score.WinType.OTHER_USER_ZI_MO.getId().equals(winType)) {
            return State.LOSE.getId();
        }
        throw CommonError.SYS_PARAM_ERROR.newException();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Integer> getPeng() {
        return peng;
    }

    public void setPeng(List<Integer> peng) {
        this.peng = peng;
    }

    public List<Integer> getGang() {
        return gang;
    }

    public void setGang(List<Integer> gang) {
        this.gang = gang;
    }

    public List<Integer> getCard() {
        return card;
    }

    public void setCard(List<Integer> card) {
        this.card = card;
    }

    public Integer getPaoShu() {
        return paoShu;
    }

    public void setPaoShu(Integer paoShu) {
        this.paoShu = paoShu;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getOtherCard() {
        return otherCard;
    }

    public void setOtherCard(Integer otherCard) {
        this.otherCard = otherCard;
    }

    public enum State {
        ZI_MO(1, "自摸"),
        JIE_PAO(2, "接炮"),
        LOSE(3, "输");

        private Integer id;
        private String name;

        State(Integer id, String name) {

            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
