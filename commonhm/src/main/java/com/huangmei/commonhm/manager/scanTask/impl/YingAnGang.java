package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.scanTask.AbstractGangScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扫描是否可以硬暗杠
 */
public class YingAnGang extends AbstractGangScanTask {

    @Override
    public Operate getOperate() {
        return Operate.YING_AN_GANG;
    }

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {

        // 手牌加上摸到的牌，判断有没有4只相同的牌
        List<Mahjong> mahjongList = new ArrayList<>(personalCardInfo.getHandCards());
        mahjongList.add(specifiedMahjong);

        // 宝娘暗杠
        mahjongList.add(mahjongGameData.getBaoMother());

        Map<String, List<Mahjong>> numbers = new HashMap<>();
        for (Mahjong mahjong : mahjongList) {
            String numberStr = mahjong.getNumber().toString();

            List<Mahjong> temps = numbers.get(numberStr);
            if (temps == null) {
                temps = new ArrayList<>(4);
                numbers.put(numberStr, temps);
            }

            temps.add(mahjong);
        }

        for (List<Mahjong> mahjongs : numbers.values()) {
            if (mahjongs.size() == 4) {
                return true;
            }
        }


        return false;
    }

}
