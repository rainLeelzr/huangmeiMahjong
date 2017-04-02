package com.huangmei.commonhm.model.mahjong;

import java.util.List;

public class Combo {

    public Type type;

    public YingRuan yingRuan;

    public List<Mahjong> mahjongs;

    public YingRuan getYingRuan() {
        return yingRuan;
    }

    public void setYingRuan(YingRuan yingRuan) {
        this.yingRuan = yingRuan;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<Mahjong> getMahjongs() {
        return mahjongs;
    }

    public void setMahjongs(List<Mahjong> mahjongs) {
        this.mahjongs = mahjongs;
    }

    public enum Type {
        // 杠
        AAAA(),

        // 碰
        AAA(),

        // 顺子
        ABC(),

        // 眼
        AA();

        Type() {
        }
    }
}
