package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.abs.AbstractHuScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.*;

/**
 * 扫描是否硬碰碰胡
 */
public class YingPengPengHu extends AbstractHuScanTask {

    /**
     * 判断依据：
     * 按花字号分组
     * 1：除了含有眼的哪，每组元素个数为3的整数倍数
     */
    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo) throws InstantiationException, IllegalAccessException {
        log.debug("座位{}进行硬碰碰胡扫描！", personalCardInfo.getRoomMember().getSeat());

        List<Mahjong> handCards = new ArrayList<>(personalCardInfo.getHandCards());
        handCards.add(putOutMahjong);
        return isPengPengHu(handCards);
    }
}
