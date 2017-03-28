package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.abs.AbstractGangScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描是否可以软大明杠
 */
public class RuanDaMingGang extends AbstractGangScanTask {

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException {
        log.debug("座位{}进行软大明杠扫描！", personalCardInfo.getRoomMember().getSeat());

        List<Mahjong> handCards = new ArrayList<>(personalCardInfo.getHandCards());
        List<Mahjong> myBaoMahjongs = getMyBaoMahjongs(handCards);
        // 如果没有宝牌，则不能软大明杠
        if (myBaoMahjongs.size() == 0) {
            return false;
        }

        // 把宝牌全部变成打出的牌，判断玩家手牌有没有三只与putOutMahjong相同的牌
        for (Mahjong myBaoMahjong : myBaoMahjongs) {
            handCards.remove(myBaoMahjong);
            handCards.add(putOutMahjong);
        }

        int match = 0;
        for (Mahjong mahjong : handCards) {
            if (mahjong.getNumber().equals(putOutMahjong.getNumber())) {
                match++;
            }
            if (match == 3) {
                return true;
            }
        }
        return false;
    }
}
