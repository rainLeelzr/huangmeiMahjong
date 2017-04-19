package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.scanTask.AbstractGangScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

/**
 * 扫描是否可以硬大明杠
 */
public class YingDaMingGang extends AbstractGangScanTask {

    @Override
    public Operate getOperate() {
        return Operate.YING_DA_MING_GANG;
    }

    // 是否已经有碰扫描出来有人可以硬杠
    private boolean hasGang = false;

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {
        if (hasGang) {
            return false;
        }

        // 判断玩家手牌有没有三只与putOutMahjong相同的牌
        int match = 0;
        for (Mahjong mahjong : personalCardInfo.getHandCards()) {
            if (mahjong.getNumber().equals(specifiedMahjong.getNumber())) {
                match++;
            }

            //如果有一张牌为宝娘，则三张牌亦可暗杠，明杠依旧如此
            if (match == 2 &&
                    specifiedMahjong.getNumber().equals(mahjongGameData.getBaoMother().getNumber())) {
                hasGang = true;
                return true;
            }

            if (match == 3) {
                hasGang = true;
                return true;
            }
        }


        return false;
    }
}
