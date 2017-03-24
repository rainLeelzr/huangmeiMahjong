package com.huangmei.commonhm.util;

/**
 * 接口协议号
 **/
public enum PidValue {

    /**
     * 系统/
     */
    CHECK_CONNECTION(9000, "检测连接"),
    TEST(1, "开发调试"),

    /**
     * 用户
     */
    LOGIN(1000, "用户登录"),
    GET_USER(1006, "用户信息"),
    GET_STANDINGS(1008, "获取战绩"),
    LONOUT(1001,"注销登录"),

    /**
     * 房间
     */
    CREATE_ROOM(2000, "创建房间"),
    JOIN_ROOM(2001, "加入房间"),
    OUT_ROOM(2002, "退出房间"),
    READY(2003, "用户准备"),
    START_GAME(2004, "开始游戏"),

    /**
     * 游戏过程
     */
    PUT_OUT_CARD(3000, "出牌"),;

    private int pid;

    private String name;

    PidValue(int pid, String name) {
        this.pid = pid;
        this.name = name;
    }

    public int getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }
}
