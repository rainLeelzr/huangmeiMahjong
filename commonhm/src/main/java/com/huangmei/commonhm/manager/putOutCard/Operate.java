package com.huangmei.commonhm.manager.putOutCard;

/**
 * 别人出牌后其他玩家可以的操作
 * 如吃胡、大明杠、碰
 */
public enum Operate {

    YING_CHI_HU(111, 11, "硬吃胡"),
    RUAN_CHI_HU(112, 11, "软吃胡"),

    YING_DA_MING_GANG(121, 12, "硬大明杠"),
    RUAN_DA_MING_GANG(122, 12, "软大明杠"),

    YING_PENG(131, 13, "硬碰"),
    RUAN_PENG(132, 13, "软碰"),;

    /**
     * 操作唯一id
     */
    private int id;

    /**
     * 操作的大类型
     */
    private int number;

    /**
     * 操作的名称
     */
    private String name;

    Operate(int id, int number, String name) {
        this.id = id;
        this.number = number;
        this.name = name;
    }
}
