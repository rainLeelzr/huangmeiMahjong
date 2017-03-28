package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.scanTask.AbstractHuScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 扫描是否软碰碰胡
 */
public class RuanPengPengHu extends AbstractHuScanTask {

    @Override
    public Operate getOperate() {
        return Operate.RUAN_CHI_HU;
    }


    /**
     * 判断依据：
     * 按花字号分组
     * 1：除了含有眼的哪，每组元素个数为3的整数倍数
     */
    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo) throws InstantiationException, IllegalAccessException {
        List<Mahjong> handCards = new ArrayList<>(personalCardInfo.getHandCards());
        handCards.add(putOutMahjong);

        List<Mahjong> myBaoMahjongs = getMyBaoMahjongs(handCards);

        // 如果没有宝牌，则不能软碰碰胡
        if (myBaoMahjongs.size() == 0) {
            return false;
        }

        // 创建用于笛卡尔的集合
        List<List<Mahjong>> baoMahjongs = new ArrayList<>(myBaoMahjongs.size());
        for (Mahjong myBaoMahjong : myBaoMahjongs) {
            List<Mahjong> m = new ArrayList<>(
                    getMahjongGameData().getBaoMahjongMakeUpMahjongs());
            m.add(myBaoMahjong);
            baoMahjongs.add(m);
        }
        // 用到笛卡尔积
        List<List<Mahjong>> circulateResult = circulate(baoMahjongs);
        for (List<Mahjong> mahjongs : circulateResult) {
            Collections.sort(handCards);
            //log.debug("座位{}进行软碰碰胡扫描宝牌变换前：{}",
            //        personalCardInfo.getRoomMember().getSeat(),
            //        handCards
            //);

            for (int i = 0; i < myBaoMahjongs.size(); i++) {
                handCards.remove(myBaoMahjongs.get(i));
                handCards.add(mahjongs.get(i));
            }
            Collections.sort(handCards);
            //log.debug("座位{}进行软碰碰胡对扫描宝牌变换后：{}",
            //        personalCardInfo.getRoomMember().getSeat(),
            //        handCards
            //);
            if (isPengPengHu(handCards)) {
                return true;
            }

            for (int i = 0; i < myBaoMahjongs.size(); i++) {
                handCards.remove(mahjongs.get(i));
                handCards.add(myBaoMahjongs.get(i));
            }
        }

        return false;

    }
}
