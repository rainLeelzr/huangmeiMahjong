package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.scanTask.AbstractGangScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import com.huangmei.commonhm.model.mahjong.algorithm.Combo;

import java.util.List;

/**
 * 扫描是否可以软加杠
 */
public class RuanJiaGang extends AbstractGangScanTask {

    @Override
    public Operate getOperate() {
        return Operate.YING_JIA_GANG;
    }

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {

        // 判断玩家有没有已经碰了的牌并且碰牌中有宝牌
        List<Combo> pengs = personalCardInfo.getPengs();
        if (pengs.size() == 0) {
            return false;
        }

        Integer baoMahjongNumber = this.mahjongGameData.getBaoMahjongs().get(0).getNumber();
        for (Combo peng : pengs) {
            // 考虑宝牌归位的情况
            int baoMahjong = 0;
            for (Mahjong mahjong : peng.getMahjongs()) {
                if (mahjong.getNumber().equals(baoMahjongNumber)) {
                    baoMahjong++;
                }
            }

            if (baoMahjong == 3) {
                //宝牌归位,不算软加杠
                return false;
            } else if (baoMahjong == 0) {
                // 没有宝牌
                return false;
            }

            if (peng.getMahjongs().get(0).getNumber().equals(
                    putOutMahjong.getNumber())) {
                return true;
            }

        }

        return false;
    }

}
