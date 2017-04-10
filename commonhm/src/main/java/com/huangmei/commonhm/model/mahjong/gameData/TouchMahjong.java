package com.huangmei.commonhm.model.mahjong.gameData;

import com.huangmei.commonhm.model.mahjong.Mahjong;

/**
 * 摸到的麻将
 */
public class TouchMahjong {

    /**
     * 摸到的麻将
     */
    private Mahjong mahjong;

    /**
     * 摸麻将的玩家id
     */
    private Integer userId;

    /**
     * 摸麻将的方式
     */
    private Integer type;

    public Mahjong getMahjong() {
        return mahjong;
    }

    public void setMahjong(Mahjong mahjong) {
        this.mahjong = mahjong;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public static enum Type {
        COMMON(1, "普通"),
        GANG(2, "杠");

        private Integer id;

        private String name;

        Type(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
