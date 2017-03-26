package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.abs.AbstractHuScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 扫描是否硬七对
 */
public class YingQiDuiHu extends AbstractHuScanTask {

    @Override
    public boolean doScan(PersonalCardInfo personalCardInfo) throws InstantiationException, IllegalAccessException {
        // todo 判断是否已经有碰，是则肯定不是七对

        // todo 判断是否已经有杠，是则肯定不是七对

        Set<Mahjong> handCards = new HashSet<>(personalCardInfo.getHandCards());
        handCards.add(putOutMahjong);

        // 按字号分组
        Map<Integer, Set<Mahjong>> ziHaoMahjongs = groupByZiHao(handCards);
        // 如果字号组内元素个数等于1, 3, 5, 7, 9, 11, 13,则肯定不是七对
        int[] elementSize = new int[]{1, 3, 5, 7, 9, 11, 13};
        for (Set<Mahjong> mahjongs : ziHaoMahjongs.values()) {
            for (int size : elementSize) {
                if (size == mahjongs.size()) {
                    return false;
                }
            }

            // 按麻将牌的号码分组
            Map<Integer, Set<Mahjong>> sameNumberMahjong = groupByNumber(mahjongs);
            int[] numberCount = new int[]{1, 3};
            for (Set<Mahjong> ms : sameNumberMahjong.values()) {
                // 如果号组内元素个数等于1, 3,则肯定不是七对
                for (int count : numberCount) {
                    if (count == ms.size()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }


}
