package com.huangmei.commonhm.model;

public class Score implements Entity {

    private static final long serialVersionUID = 1L;

    /**  */
    protected Integer id;

    /**  */
    protected Integer anGangTimes;

    /**  */
    protected Integer coin;

    /**  */
    protected Integer dianPaoUserId;

    /**  */
    protected Integer isZiMo;

    /**  */
    protected Integer jiePaoUserId;

    /**  */
    protected Integer mingGangTimes;

    /**  */
    protected Integer roomId;

    /**  */
    protected Integer score;
    /**  */
    protected Integer paoNum;

    /**
     * 局数
     */
    protected Integer times;

    /**  */
    protected Integer type;

    /**  */
    protected Integer userId;

    /**
     * 对局状况
     */
    protected Integer winType;

    /**
     * 胡牌牌型
     */
    protected Integer huType;

    protected java.util.Date createdTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAnGangTimes() {
        return anGangTimes;
    }

    public void setAnGangTimes(Integer anGangTimes) {
        this.anGangTimes = anGangTimes;
    }

    public Integer getCoin() {
        return coin;
    }

    public void setCoin(Integer coin) {
        this.coin = coin;
    }

    public Integer getDianPaoUserId() {
        return dianPaoUserId;
    }

    public void setDianPaoUserId(Integer dianPaoUserId) {
        this.dianPaoUserId = dianPaoUserId;
    }

    public Integer getIsZiMo() {
        return isZiMo;
    }

    public void setIsZiMo(Integer isZiMo) {
        this.isZiMo = isZiMo;
    }

    public Integer getJiePaoUserId() {
        return jiePaoUserId;
    }

    public void setJiePaoUserId(Integer jiePaoUserId) {
        this.jiePaoUserId = jiePaoUserId;
    }

    public Integer getMingGangTimes() {
        return mingGangTimes;
    }

    public void setMingGangTimes(Integer mingGangTimes) {
        this.mingGangTimes = mingGangTimes;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getScore() {
        return score;
    }

    public Integer getPaoNum() {
        return paoNum;
    }

    public void setPaoNum(Integer paoNum) {
        this.paoNum = paoNum;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getWinType() {
        return winType;
    }

    public void setWinType(Integer winType) {
        this.winType = winType;
    }

    public Integer getHuType() {
        return huType;
    }

    public void setHuType(Integer huType) {
        this.huType = huType;
    }

    public java.util.Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(java.util.Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("id = ").append(id).append(", ");
        builder.append("anGangTimes = ").append(anGangTimes).append(", ");
        builder.append("coin = ").append(coin).append(", ");
        builder.append("dianPaoUserId = ").append(dianPaoUserId).append(", ");
        builder.append("isZiMo = ").append(isZiMo).append(", ");
        builder.append("jiePaoUserId = ").append(jiePaoUserId).append(", ");
        builder.append("mingGangTimes = ").append(mingGangTimes).append(", ");
        builder.append("roomId = ").append(roomId).append(", ");
        builder.append("score = ").append(score).append(", ");
        builder.append("paoNum = ").append(paoNum).append(", ");
        builder.append("times = ").append(times).append(", ");
        builder.append("type = ").append(type).append(", ");
        builder.append("userId = ").append(userId).append(", ");
        builder.append("winType = ").append(winType).append(", ");
        builder.append("huType = ").append(huType).append(", ");
        builder.append("createdTime = ").append(createdTime).append(", ");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Score other = (Score) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}