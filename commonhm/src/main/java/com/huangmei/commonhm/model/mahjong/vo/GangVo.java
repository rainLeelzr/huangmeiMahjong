package com.huangmei.commonhm.model.mahjong.vo;

import com.huangmei.commonhm.model.mahjong.Combo;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.type.GangType;

import java.util.ArrayList;
import java.util.List;

public class GangVo {

    private List<Integer> mahjongIds;

    private Integer type;

    public GangVo() {
    }

    public GangVo(List<Integer> mahjongIds, Integer type) {
        this.mahjongIds = mahjongIds;
        this.type = type;
    }

    public static List<GangVo> parseFromGangCombos(List<Combo> combos) {
        List<GangVo> gangVos = new ArrayList<>(combos.size());
        for (Combo combo : combos) {
            GangVo gangVo = new GangVo(
                    Mahjong.parseToIds(combo.getMahjongs()),
                    GangType.parseFromPidValue(combo.getPidValue()).getId()
            );
            gangVos.add(gangVo);
        }
        return gangVos;
    }

    public List<Integer> getMahjongIds() {
        return mahjongIds;
    }

    public void setMahjongIds(List<Integer> mahjongIds) {
        this.mahjongIds = mahjongIds;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
