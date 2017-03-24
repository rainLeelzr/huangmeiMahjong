package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.AbstractGangScanTask;
import com.huangmei.commonhm.model.mahjong.BaseOperate;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.List;
import java.util.Set;

/**
 * 扫描是否可以硬大明杠
 */
public class YingDaMingGang extends AbstractGangScanTask {

    // 是否已经有碰扫描出来有人可以硬杠
    private boolean hasGang = false;

    @Override
    public void scan() throws InstantiationException, IllegalAccessException {
        // 循环除了出牌的玩家，判断有没有硬杠
        List<PersonalCardInfo> personalCardInfos = mahjongGameData.getPersonalCardInfos();
        for (PersonalCardInfo personalCardInfo : personalCardInfos) {
            //log.debug("扫描{}前座位{}的手牌：{}{}",
            //        getBaseOperate().getName(),
            //        personalCardInfo.getRoomMember().getSeat(),
            //        personalCardInfo.getHandCards().size(),
            //        personalCardInfo.getHandCards());

            if (!user.getId().equals(
                    personalCardInfo.getRoomMember().getUserId())) {
                if (canGang(personalCardInfo)) {
                    // 添加杠的可行操作
                    Set<BaseOperate> myOperates = getMyOperates(
                            personalCardInfo.getRoomMember().getUserId());
                    myOperates.add(getBaseOperate());
                }
            }
        }
    }


    private boolean canGang(PersonalCardInfo personalCardInfo) {
        if(hasGang){
            return false;
        }

        // 判断玩家手牌有没有三只与putOutMahjong相同的牌
        int match = 0;
        for (Mahjong mahjong : personalCardInfo.getHandCards()) {
            if (mahjong.getNumber().equals(putOutMahjong.getNumber())) {
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
