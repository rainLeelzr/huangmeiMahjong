package com.huangmei.commonhm.manager.operate;

import com.huangmei.commonhm.util.PidValue;

/**
 * 别人出牌后其他玩家可以的操作
 * 如吃胡、大明杠、碰
 */
public enum Operate {

    YING_CHI_HU(PidValue.YING_CHI_HU, BaseOperate.HU, "硬吃胡"),
    RUAN_CHI_HU(PidValue.RUAN_CHI_HU, BaseOperate.HU, "软吃胡"),

    YING_AN_GANG(PidValue.YING_AN_GANG, BaseOperate.GANG, "硬暗杠"),
    RUAN_AN_GANG(PidValue.RUAN_AN_GANG, BaseOperate.GANG, "软暗杠"),

    YING_DA_MING_GANG(PidValue.YING_DA_MING_GANG, BaseOperate.GANG, "硬大明杠"),
    RUAN_DA_MING_GANG(PidValue.RUAN_DA_MING_GANG, BaseOperate.GANG, "软大明杠"),

    YING_JIA_GANG(PidValue.YING_JIA_GANG, BaseOperate.GANG, "硬加杠"),
    RUAN_JIA_GANG(PidValue.RUAN_JIA_GANG, BaseOperate.GANG, "软加杠"),

    YING_PENG(PidValue.YING_PENG, BaseOperate.PENG, "硬碰"),
    RUAN_PENG(PidValue.RUAN_PENG, BaseOperate.PENG, "软碰"),;

    /**
     * 操作唯一id
     */
    private PidValue pidValue;

    /**
     * 操作的大类型
     */
    private BaseOperate baseOperate;
    /**
     * 操作的名称
     */
    private String name;

    Operate(PidValue pidValue, BaseOperate baseOperate, String name) {
        this.pidValue = pidValue;
        this.baseOperate = baseOperate;
        this.name = name;
    }

    public PidValue getPidValue() {
        return pidValue;
    }

    public void setPidValue(PidValue pidValue) {
        this.pidValue = pidValue;
    }

    public BaseOperate getBaseOperate() {
        return baseOperate;
    }

    public void setBaseOperate(BaseOperate baseOperate) {
        this.baseOperate = baseOperate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
