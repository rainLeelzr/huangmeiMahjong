package com.huangmei.commonhm.model;

import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.util.CommonError;

public class Score implements Entity {

    private static final long serialVersionUID = 1L;

    /**  */
    protected Integer id;

    /**  */
    protected Integer anGangTimes;

    /**  */
    protected Integer coin;

    /**
     * 点炮的用户id
     */
    protected Integer dianPaoUserId;

    /**  */
    protected Integer isZiMo;

    /**
     * 接炮的用户id
     */
    protected Integer jiePaoUserId;

    /**  */
    protected Integer mingGangTimes;

    /**  */
    protected Integer roomId;

    /**
     * 分数
     */
    protected Integer score;

    /**
     * 炮数
     */
    protected Integer paoNum;

    /**
     * 局数
     */
    protected Integer times;

    /**
     * 金币房、好友房
     */
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

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getPaoNum() {
        return paoNum;
    }

    public void setPaoNum(Integer paoNum) {
        this.paoNum = paoNum;
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

    public enum WinType {
        NONE(1, "没有胡牌,不用输分"),
        ZI_MO(2, "自摸"),
        DIAN_PAO(3, "点炮"),
        JIE_PAO(4, "接炮"),
        OTHER_USER_ZI_MO(5, "别家自摸");

        private Integer id;
        private String name;

        WinType(Integer id, String name) {
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

    public enum HuType {
        PING_HU(1, 0, "平胡"),
        PING_HU_GANG_SHANG_HUA(2, 5, "平胡杠上开花"),
        PENG_PENG_HU(3, 10, "碰碰胡"),
        QI_DUI(4, 10, "七对"),
        QING_YI_SE(5, 15, "清一色"),
        PENG_PENG_HU_GANG_SHANG_HUA(6, 15, "碰碰胡杠上开花"),
        QI_DUI_GANG_SHANG_HUA(7, 15, "七对杠上开花"),
        QING_YI_SE_GANG_SHANG_HUA(8, 20, "清一色杠上开花"),
        QING_YI_SE_PENG_PENG_HU(9, 25, "清一色碰碰胡"),
        QING_YI_SE_QI_DUI(10, 25, "清一色七对"),
        QING_YI_SE_PENG_PENG_HU_GANG_SHANG_HUA(11, 30, "清一色碰碰胡杠上开花"),
        QING_YI_SE_QI_DUI_GANG_SHANG_HUA(12, 30, "清一色七对杠上开花");

        private Integer id;
        private Integer paoNum;
        private String name;

        HuType(Integer id, Integer paoNum, String name) {
            this.id = id;
            this.paoNum = paoNum;
            this.name = name;
        }

        public static HuType parse(Operate operate) {
            if (operate == null) {
                throw CommonError.SYS_PARAM_ERROR.newException();
            }

            HuType result = null;
            switch (operate) {
                case ZI_MO_YING_PENG_PENG_HU:
                    result = PENG_PENG_HU;
                    break;
                case ZI_MO_RUAN_PENG_PENG_HU:
                    result = PENG_PENG_HU;
                    break;
                case ZI_MO_YING_QI_DUI_HU:
                    result = QI_DUI;
                    break;
                case ZI_MO_RUAN_QI_DUI_HU:
                    result = QI_DUI;
                    break;
                case ZI_MO_YING_PING_HU:
                    result = PING_HU;
                    break;
                case ZI_MO_RUAN_PING_HU:
                    result = PING_HU;
                    break;
                case CHI_YING_PENG_PENG_HU:
                    result = PENG_PENG_HU;
                    break;
                case CHI_RUAN_PENG_PENG_HU:
                    result = PENG_PENG_HU;
                    break;
                case CHI_YING_QI_DUI_HU:
                    result = QI_DUI;
                    break;
                case CHI_RUAN_QI_DUI_HU:
                    result = QI_DUI;
                    break;
                case CHI_YING_PING_HU:
                    result = PING_HU;
                    break;
                case CHI_RUAN_PING_HU:
                    result = PING_HU;
                    break;
                default:
                    throw CommonError.SYS_PARAM_ERROR.newException();
            }
            return result;
        }

        public boolean isBigHu() {
            switch (this) {
                case PING_HU_GANG_SHANG_HUA:
                    return true;
                case PENG_PENG_HU:
                    return true;
                case QI_DUI:
                    return true;
                case QING_YI_SE:
                    return true;
                case PENG_PENG_HU_GANG_SHANG_HUA:
                    return true;
                case QI_DUI_GANG_SHANG_HUA:
                    return true;
                case QING_YI_SE_GANG_SHANG_HUA:
                    return true;
                case QING_YI_SE_PENG_PENG_HU:
                    return true;
                case QING_YI_SE_QI_DUI:
                    return true;
                case QING_YI_SE_PENG_PENG_HU_GANG_SHANG_HUA:
                    return true;
                case QING_YI_SE_QI_DUI_GANG_SHANG_HUA:
                    return true;
                default:
                    return false;
            }
        }

        public Integer getPaoNum() {
            return paoNum;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public enum IsZiMo {
        ZI_MO(1, "自摸"),
        NOT_ZI_MO(2, "非自摸");

        private Integer id;
        private String name;

        IsZiMo(Integer id, String name) {
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