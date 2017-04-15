package com.huangmei.commonhm.model.mahjong.vo;

/**
 * 总结算界面，每个玩家的信息
 */
public class SingleUserGameScoreVo {

    private String nickName;
    private Integer uId;
    private String image;

    private Integer ziMo;
    private Integer dianPao;
    private Integer jiePao;
    private Integer gang;
    private Integer anGang;
    private Integer score;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getuId() {
        return uId;
    }

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getZiMo() {
        return ziMo;
    }

    public void setZiMo(Integer ziMo) {
        this.ziMo = ziMo;
    }

    public Integer getDianPao() {
        return dianPao;
    }

    public void setDianPao(Integer dianPao) {
        this.dianPao = dianPao;
    }

    public Integer getJiePao() {
        return jiePao;
    }

    public void setJiePao(Integer jiePao) {
        this.jiePao = jiePao;
    }

    public Integer getGang() {
        return gang;
    }

    public void setGang(Integer gang) {
        this.gang = gang;
    }

    public Integer getAnGang() {
        return anGang;
    }

    public void setAnGang(Integer anGang) {
        this.anGang = anGang;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
