package com.huangmei.commonhm.model.mahjong.algorithm;

import com.huangmei.commonhm.model.mahjong.Mahjong;

public class Combo {

    private int type;

    private Mahjong[] mahjongs;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Mahjong[] getMahjongs() {
        return mahjongs;
    }

    public void setMahjongs(Mahjong[] mahjongs) {
        this.mahjongs = mahjongs;
    }

    public enum Type {
        AAA(1),
        ABC(2);

        private int type;

        Type(int type) {

            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
