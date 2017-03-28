package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.scanTask.AbstractGangScanTask;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import com.huangmei.commonhm.model.mahjong.algorithm.Combo;

import java.util.List;

/**
 * 扫描是否可以硬加杠
 */
public class YingJiaGang extends AbstractGangScanTask {

    @Override
    public Operate getOperate() {
        return Operate.YING_JIA_GANG;
    }

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {

        // 判断玩家有没有已经碰了的牌
        List<Combo> pengs = personalCardInfo.getPengs();
        if (pengs.size() == 0) {
            return false;
        }

        for (Combo peng : pengs) {
            if (peng.getMahjongs().get(0).getNumber().equals(
                    putOutMahjong.getNumber())) {
                return true;
            }
        }

        return false;
    }

}
