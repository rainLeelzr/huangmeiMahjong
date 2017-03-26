package com.huangmei.commonhm.manager.scanTask.abs;


import com.huangmei.commonhm.manager.scanTask.ScanTask;
import com.huangmei.commonhm.model.mahjong.BaseOperate;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.algorithm.Combo;

import java.util.*;

/**
 * 扫描用户是否可以吃胡
 */
public abstract class AbstractHuScanTask extends ScanTask {


    private static int[] noHuSize = new int[]{1, 4, 7, 10, 13};

    @Override
    public BaseOperate getBaseOperate() {
        return BaseOperate.HU;
    }

    protected boolean isPinghu(Set<Mahjong> handCards) {
        // 按麻将的字号分组
        Map<Integer, Set<Mahjong>> ziHaoMahjongs = groupByZiHao(handCards);

        if (preCheck(ziHaoMahjongs)) {
            for (Set<Mahjong> mahjongSet : ziHaoMahjongs.values()) {
                if (!check(new ArrayList<>(mahjongSet))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    protected boolean preCheck(Map<Integer, Set<Mahjong>> ziHaoMahjongs) {
        for (Set<Mahjong> mahjongs : ziHaoMahjongs.values()) {
            // 检查每个字号组合的大小符不合胡牌大小
            for (int size : noHuSize) {
                if (size == mahjongs.size()) {
                    return false;
                }
            }

            // mahjongs.size() == 2只能为眼
            if (mahjongs.size() == 2) {
                Combo AA = AA(new ArrayList<>(mahjongs));
                if (AA == null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 根据传入的list，组成AAA、ABC、AA组合
     * 组合成功，返回true，不成功返回false
     */
    protected boolean check(List<Mahjong> mahjongs) {
        if (mahjongs.size() == 0) {
            return true;
        }

        Combo combo = AAA(mahjongs);
        if (combo == null) {
            combo = ABC(mahjongs);
            if (combo == null) {
                combo = AA(mahjongs);
                if (combo == null) {
                    return false;
                } else {
                    return check(mahjongs);
                }
            } else {
                if (mahjongs.size() == 0) {
                    return true;
                } else {
                    return check(mahjongs);
                }
            }
        } else {
            if (mahjongs.size() == 0) {
                return true;
            } else {
                return check(mahjongs);
            }
        }
    }

    /**
     * 从列表第一只为指定开始牌，找出一个AAA的组合
     */
    protected Combo AAA(List<Mahjong> mahjongs) {
        if (mahjongs.size() < 3) {
            return null;
        }

        Combo combo = new Combo();
        combo.type = Combo.Type.AAA;
        List<Mahjong> AAAMahjong = new ArrayList<>(3);
        combo.mahjongs = AAAMahjong;
        AAAMahjong.add(mahjongs.get(0));
        mahjongs.remove(0);

        for (int i = 0; i < mahjongs.size(); i++) {
            if (AAAMahjong.get(0).getDigit().equals(mahjongs.get(i).getDigit())) {
                AAAMahjong.add(mahjongs.get(i));
                mahjongs.remove(i);
                break;
            }
        }

        if (AAAMahjong.size() != 2) {
            putBackMahjongToList(combo, mahjongs);
            return null;
        }

        for (int i = 0; i < mahjongs.size(); i++) {
            if (AAAMahjong.get(0).getDigit().equals(mahjongs.get(i).getDigit())) {
                AAAMahjong.add(mahjongs.get(i));
                mahjongs.remove(i);
                break;
            }
        }

        if (AAAMahjong.size() != 3) {
            putBackMahjongToList(combo, mahjongs);
            return null;
        }

        return combo;
    }

    /**
     * 从列表第一只为指定开始牌，找出一个ABC的组合
     */
    protected Combo ABC(List<Mahjong> mahjongs) {
        if (mahjongs.size() < 3) {
            return null;
        }

        Combo combo = new Combo();
        combo.type = Combo.Type.ABC;
        List<Mahjong> ABCMahjong = new ArrayList<>(3);
        combo.mahjongs = ABCMahjong;
        ABCMahjong.add(mahjongs.get(0));
        mahjongs.remove(0);

        for (int i = 0; i < mahjongs.size(); i++) {
            if (ABCMahjong.get(0).getDigit() + 1 == mahjongs.get(i).getDigit()) {
                ABCMahjong.add(mahjongs.get(i));
                mahjongs.remove(i);
                break;
            }
        }

        if (ABCMahjong.size() != 2) {
            putBackMahjongToList(combo, mahjongs);
            return null;
        }

        for (int i = 0; i < mahjongs.size(); i++) {
            if (ABCMahjong.get(0).getDigit() + 2 == mahjongs.get(i).getDigit()) {
                ABCMahjong.add(mahjongs.get(i));
                mahjongs.remove(i);
                break;
            }
        }

        if (ABCMahjong.size() != 3) {
            putBackMahjongToList(combo, mahjongs);
            return null;
        }

        return combo;
    }

    /**
     * 从列表第一只为指定开始牌，找出一个AA的组合
     */
    protected Combo AA(List<Mahjong> mahjongs) {
        Combo combo = new Combo();
        combo.type = Combo.Type.AA;
        List<Mahjong> AAMahjong = new ArrayList<>(2);
        combo.mahjongs = AAMahjong;
        AAMahjong.add(mahjongs.get(0));
        mahjongs.remove(0);

        for (int i = 0; i < mahjongs.size(); i++) {
            if (AAMahjong.get(0).getDigit().equals(mahjongs.get(i).getDigit())) {
                AAMahjong.add(mahjongs.get(i));
                mahjongs.remove(i);
                break;
            }
        }

        if (AAMahjong.size() != 2) {
            putBackMahjongToList(combo, mahjongs);
            return null;
        }

        return combo;
    }

    private void putBackMahjongToList(Combo combo, List<Mahjong> mahjongs) {
        for (Mahjong m : combo.mahjongs) {
            mahjongs.add(m);
        }
        Collections.sort(mahjongs);
    }
}