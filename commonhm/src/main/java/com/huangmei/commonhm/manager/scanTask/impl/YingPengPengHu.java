package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.scanTask.abs.AbstractHuScanTask;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.*;

/**
 * 扫描是否硬碰碰胡
 */
public class YingPengPengHu extends AbstractHuScanTask {

    private boolean hasEye = false;// 眼，初始化没有眼。只能有一对眼

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

        // 按麻将的字号分组
        Map<Integer, List<Mahjong>> Mahjongs = groupByZiHao(handCards);

        // 判断每个字号组是否满足碰碰胡
        int[] elementSize = new int[]{1, 4, 7, 10, 13};
        for (List<Mahjong> mahjongs : Mahjongs.values()) {
            // 如果组内元素个数等于1, 4, 7, 10, 13,则肯定不是对对碰
            for (int size : elementSize) {
                if (size == mahjongs.size()) {
                    return false;
                }
            }

            // 按麻将牌的号码分组
            Map<Integer, List<Mahjong>> sameNumberMahjongs =
                    groupByNumber(mahjongs);
            int[] sameNumberMahjongElementSize = new int[]{1, 4};
            for (List<Mahjong> mahjongSet : sameNumberMahjongs.values()) {
                // 如果组内元素个数等于1, 4,则肯定不是对对碰
                for (int size : sameNumberMahjongElementSize) {
                    if (size == mahjongSet.size()) {
                        return false;
                    }
                }

                // 如果有重复的眼,肯定不是对对碰
                if (haveSecondEye(mahjongSet)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 判断是否有重复的眼
     * sameMahjong只能是2或3个相同的麻将
     */
    private boolean haveSecondEye(List<Mahjong> sameMahjong) {
        if (sameMahjong.size() == 2) {
            // 如果这个set的数量是2，说明是眼，再判断之前有没有眼
            if (hasEye) {
                return true;
            } else {
                hasEye = true;
            }
        }
        return false;
    }
}
