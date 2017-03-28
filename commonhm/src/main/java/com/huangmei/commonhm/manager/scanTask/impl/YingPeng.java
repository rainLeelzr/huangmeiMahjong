package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.abs.AbstractPengScanTask;
import com.huangmei.commonhm.model.mahjong.BaseOperate;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.Set;

/**
 * 扫描是否可以硬碰
 */
public class YingPeng extends AbstractPengScanTask {

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {
        Set<BaseOperate> myOperates = getMyOperates(
                personalCardInfo.getRoomMember().getUserId());
        // 有大明杠肯定有碰
        if(myOperates.contains(BaseOperate.GANG)){
            return true;
        }

        log.debug("座位{}进行硬碰扫描！", personalCardInfo.getRoomMember().getSeat());

        // 判断玩家手牌有没有两只与putOutMahjong相同的牌
        int match = 0;
        for (Mahjong mahjong : personalCardInfo.getHandCards()) {
            if (mahjong.getNumber().equals(putOutMahjong.getNumber())) {
                match++;
            }
            if (match == 2) {
                return true;
            }
        }
        return false;
    }
}
