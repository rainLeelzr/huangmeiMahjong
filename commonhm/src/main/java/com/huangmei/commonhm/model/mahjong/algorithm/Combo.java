package com.huangmei.commonhm.model.mahjong.algorithm;

import com.huangmei.commonhm.model.mahjong.Mahjong;

import java.util.List;

public class Combo {

    public Type type;

    public List<Mahjong> mahjongs;

    public enum Type {
        AAA(),
        ABC(),
        AA();

        Type() {
        }
    }
}
