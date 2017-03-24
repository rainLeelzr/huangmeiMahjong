//package com.huangmei.commonhm.manager.scanTask;
//
//import com.huangmei.commonhm.manager.putOutCard.Operate;
//import com.huangmei.commonhm.model.mahjong.Mahjong;
//import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
//
//import java.util.List;
//import java.util.Set;
//
///**
// * 是否有硬清一色七对
// */
//public class YingQingYiSeQiDui extends AbstractHuScanTask {
//
//    //万字筒字条字,3种字可以清一色。番字不能清一色
//    private static int[] canYingQingYiSeMahjongZi =
//            {Mahjong.WANG_ZI, Mahjong.TONG_ZI, Mahjong.TIAO_ZI};
//
//    // 本任务所扫描的操作是硬吃胡
//    private static Operate belongToOperate = Operate.YING_CHI_HU;
//
//    @Override
//    public void scan()
//            throws InstantiationException, IllegalAccessException {
//        List<PersonalCardInfo> personalCardInfos = mahjongGameData
//                .getPersonalCardInfos();
//        // 遍历非出牌的玩家，判断是否能有吃胡操作
//        for (PersonalCardInfo personalCardInfo : personalCardInfos) {
//            if (!personalCardInfo.getRoomMember().getUserId()
//                    .equals(user.getId())) {
//                Set<Operate> myOperates = getMyOperates(
//                        personalCardInfo.getRoomMember().getUserId());
//
//                // 如果其他操作扫描器已经扫描到本用户可以有硬吃胡的操作，
//                // 则不需要再扫描是否有硬清一色七对
//                if (myOperates.contains(belongToOperate)) {
//                    return;
//                }
//
//                if (doScan()) {
//                    // 添加硬清一色七对的可行操作
//                    myOperates.add(belongToOperate);
//                }
//
//            }
//        }
//
//
//    }
//
//    /**
//     * 判断有否硬清一色七对
//     */
//    private boolean doScan() {
//        int baseZi = putOutMahjong.getZi();
//
//        // 判断打出的牌（摸到的牌）是否万筒条其中一种
//        // 如果不是其中一，则肯定不是清一色，也肯定不是清一色七对
//        boolean isOneOfYingQingYiSeZi = false;
//        for (int i : canYingQingYiSeMahjongZi) {
//            if (i == baseZi) {
//                isOneOfYingQingYiSeZi = true;
//                break;
//            }
//        }
//        if (!isOneOfYingQingYiSeZi) {
//            return false;
//        }
//
//        // todo 判断是否已经有杠或者碰，有则不是七对，也肯定不是清一色七对
//
//        // 判断手牌与打出的牌（摸到的牌）的字号是否一样，只要有一个手牌不一样，则肯定不是清一色
//        for (Mahjong mahjong : personalCardInfo.getHandCards()) {
//            if (mahjong.getZi() != baseZi) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//}
