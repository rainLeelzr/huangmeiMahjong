package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.abs.AbstractHuScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.*;

/**
 * 扫描是否硬七对
 */
public class YingQiDuiHu extends AbstractHuScanTask {

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {
        // todome 判断是否已经有碰，是则肯定不是七对

        // todome 判断是否已经有杠，是则肯定不是七对

        log.debug("座位{}进行硬七对扫描！", personalCardInfo.getRoomMember().getSeat());
        List<Mahjong> handCards = new ArrayList<>(personalCardInfo.getHandCards());
        handCards.add(putOutMahjong);
        return isQiDui(handCards);
    }


}
