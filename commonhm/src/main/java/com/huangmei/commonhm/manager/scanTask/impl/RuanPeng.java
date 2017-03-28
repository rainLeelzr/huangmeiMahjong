package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.abs.AbstractPengScanTask;
import com.huangmei.commonhm.model.mahjong.BaseOperate;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 扫描是否可以软碰
 */
public class RuanPeng extends AbstractPengScanTask {

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {
        Set<BaseOperate> myOperates = getMyOperates(
                personalCardInfo.getRoomMember().getUserId());
        // 有大明杠肯定有碰
        if(myOperates.contains(BaseOperate.GANG)){
            return true;
        }

        log.debug("座位{}进行软碰扫描！", personalCardInfo.getRoomMember().getSeat());
        List<Mahjong> handCards = new ArrayList<>(personalCardInfo.getHandCards());
        List<Mahjong> myBaoMahjongs = getMyBaoMahjongs(handCards);
        // 如果没有宝牌，则不能软大明杠
        if (myBaoMahjongs.size() == 0) {
            return false;
        }

        // 把宝牌全部变成打出的牌，判断玩家手牌有没有两只与putOutMahjong相同的牌
        for (Mahjong myBaoMahjong : myBaoMahjongs) {
            handCards.remove(myBaoMahjong);
            handCards.add(putOutMahjong);
        }

        int match = 0;
        for (Mahjong mahjong : handCards) {
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
