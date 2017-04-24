package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.scanTask.AbstractGangScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描是否可以硬大明杠
 */
public class YingDaMingGang extends AbstractGangScanTask {

    // 是否已经有碰扫描出来有人可以硬杠
    private boolean hasGang = false;

    @Override
    public Operate getOperate() {
        return Operate.YING_DA_MING_GANG;
    }

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {
        if (hasGang) {
            return false;
        }

        List<Mahjong> handCards = new ArrayList<>(personalCardInfo.getHandCards());

        // 宝娘杠
        if (specifiedMahjong.getNumber().equals(mahjongGameData.getBaoMother().getNumber())) {
            handCards.add(mahjongGameData.getBaoMother());
        }

        // 判断玩家手牌有没有三只与putOutMahjong相同的牌
        int match = 0;
        for (Mahjong mahjong : handCards) {
            if (mahjong.getNumber().equals(specifiedMahjong.getNumber())) {
                match++;
            }

            if (match == 3) {
                hasGang = true;
                return true;
            }
        }


        return false;
    }
}
