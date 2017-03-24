package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.AbstractPengScanTask;
import com.huangmei.commonhm.model.mahjong.BaseOperate;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.List;
import java.util.Set;

/**
 * 扫描是否可以硬碰
 */
public class YingPeng extends AbstractPengScanTask {

    // 是否已经有碰扫描出来有人可以硬碰
    private boolean hasPeng = false;

    @Override
    public void scan() throws InstantiationException, IllegalAccessException {
        // 循环除了出牌的玩家，判断有没有硬碰
        List<PersonalCardInfo> personalCardInfos = mahjongGameData.getPersonalCardInfos();
        for (PersonalCardInfo personalCardInfo : personalCardInfos) {
            //log.debug("扫描{}前座位{}的手牌：{}{}",
            //        getBaseOperate().getName(),
            //        personalCardInfo.getRoomMember().getSeat(),
            //        personalCardInfo.getHandCards().size(),
            //        personalCardInfo.getHandCards());

            if (!user.getId().equals(
                    personalCardInfo.getRoomMember().getUserId())) {
                if (canPeng(personalCardInfo)) {
                    // 添加碰的可行操作
                    Set<BaseOperate> myOperates = getMyOperates(
                            personalCardInfo.getRoomMember().getUserId());
                    myOperates.add(getBaseOperate());
                }
            }
        }
    }


    private boolean canPeng(PersonalCardInfo personalCardInfo) {
        if(hasPeng){
            return false;
        }

        // 判断玩家手牌有没有两只与putOutMahjong相同的牌
        int match = 0;
        for (Mahjong mahjong : personalCardInfo.getHandCards()) {
            if (mahjong.getNumber().equals(putOutMahjong.getNumber())) {
                match++;
            }
            if (match == 2) {
                hasPeng = true;
                return true;
            }
        }
        return false;
    }


}
