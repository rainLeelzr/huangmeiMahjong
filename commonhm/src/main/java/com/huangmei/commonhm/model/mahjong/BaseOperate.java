package com.huangmei.commonhm.model.mahjong;

/**
 * 基本操作类型
 */
public enum BaseOperate {
    PENG("碰"),
    GANG("杠"),

    HU("胡");//胡包括自摸、吃胡

    private String name;

    BaseOperate(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "{\"BaseOperate\":{"
                + "\"name\":\"" + name + "\""
                + "}}";
    }
}
