package com.huangmei.commonhm.manager.operate;

import com.huangmei.commonhm.util.PidValue;

/**
 * 别人出牌后其他玩家可以的操作
 * 如吃胡、大明杠、碰
 */
public enum Operate {

    ZI_MO_YING_PENG_PENG_HU(PidValue.YING_ZI_MO, BaseOperate.HU, "自摸硬碰碰胡"),
    ZI_MO_RUAN_PENG_PENG_HU(PidValue.RUAN_ZI_MO, BaseOperate.HU, "自摸软碰碰胡"),

    ZI_MO_YING_QI_DUI_HU(PidValue.YING_ZI_MO, BaseOperate.HU, "自摸硬七对胡"),
    ZI_MO_RUAN_QI_DUI_HU(PidValue.RUAN_ZI_MO, BaseOperate.HU, "自摸软七对胡"),

    ZI_MO_YING_PING_HU(PidValue.YING_ZI_MO, BaseOperate.HU, "自摸硬平胡"),
    ZI_MO_RUAN_PING_HU(PidValue.RUAN_ZI_MO, BaseOperate.HU, "自摸软平胡"),

    CHI_YING_PENG_PENG_HU(PidValue.YING_CHI_HU, BaseOperate.HU, "吃硬碰碰胡"),
    CHI_RUAN_PENG_PENG_HU(PidValue.RUAN_CHI_HU, BaseOperate.HU, "吃软碰碰胡"),

    CHI_YING_QI_DUI_HU(PidValue.YING_CHI_HU, BaseOperate.HU, "吃硬七对胡"),
    CHI_RUAN_QI_DUI_HU(PidValue.RUAN_CHI_HU, BaseOperate.HU, "吃软七对胡"),

    CHI_YING_PING_HU(PidValue.YING_CHI_HU, BaseOperate.HU, "吃硬平胡"),
    CHI_RUAN_PING_HU(PidValue.RUAN_CHI_HU, BaseOperate.HU, "吃软平胡"),

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
