package com.huangmei.commonhm.manager.scanTask;

import com.huangmei.commonhm.manager.putOutCard.AfterPutOutCardOperate;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.BaseOperate;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 扫描任务接口
 */
public abstract class ScanTask {

    protected static final Logger log = LoggerFactory.getLogger
            (ScanTask.class);

    /**
     * 用户打出的牌
     */
    protected Mahjong putOutMahjong;
    /**
     * 出牌的玩家
     */
    protected User user;
    private MahjongGameData mahjongGameData;
    /**
     * 具体的任务扫描器判定到某个用户可以执行某些操作时，向此列表添加元素
     */
    private List<AfterPutOutCardOperate> canOperates;

    /**
     * 基本操作类型
     */
    public abstract BaseOperate getBaseOperate();

    public void scan()
            throws InstantiationException, IllegalAccessException {
        // 循环除了出牌的玩家，判断能不能有一些操作
        List<PersonalCardInfo> personalCardInfos = mahjongGameData.getPersonalCardInfos();
        for (PersonalCardInfo personalCardInfo : personalCardInfos) {
            //log.debug("扫描{}前座位{}的手牌：{}{}",
            //        getBaseOperate().getName(),
            //        personalCardInfo.getRoomMember().getSeat(),
            //        personalCardInfo.getHandCards().size(),
            //        personalCardInfo.getHandCards());

            if (!user.getId().equals(
                    personalCardInfo.getRoomMember().getUserId())) {
                Set<BaseOperate> myOperates = getMyOperates(
                        personalCardInfo.getRoomMember().getUserId());
                if (!myOperates.contains(getBaseOperate())) {
                    if (doScan(personalCardInfo)) {
                        // 添加可行操作
                        myOperates.add(getBaseOperate());
                    }
                }
            }
        }
    }

    public abstract boolean doScan(PersonalCardInfo personalCardInfo)
            throws InstantiationException, IllegalAccessException;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<AfterPutOutCardOperate> getCanOperates() {
        return canOperates;
    }

    public void setCanOperates(List<AfterPutOutCardOperate> canOperates) {
        this.canOperates = canOperates;
    }

    public MahjongGameData getMahjongGameData() {
        return mahjongGameData;
    }

    public void setMahjongGameData(MahjongGameData mahjongGameData) {
        this.mahjongGameData = mahjongGameData;
    }

    public Mahjong getPutOutMahjong() {
        return putOutMahjong;
    }

    public void setPutOutMahjong(Mahjong putOutMahjong) {
        this.putOutMahjong = putOutMahjong;
    }

    /**
     * 获取本玩家的可行的操作列表
     */
    private Set<BaseOperate> getMyOperates(Integer userId) {
        // 找出userId的个人牌信息
        PersonalCardInfo personalCardInfo = null;
        for (PersonalCardInfo cardInfo : mahjongGameData.getPersonalCardInfos()) {
            if (cardInfo.getRoomMember().getUserId().equals(userId)) {
                personalCardInfo = cardInfo;
                break;
            }
        }

        // 找出userId的可行的操作列表
        AfterPutOutCardOperate afterPutOutCardOperate = null;
        for (AfterPutOutCardOperate canOperate : canOperates) {
            if (canOperate.getRoomMember().getUserId().equals(userId)) {
                afterPutOutCardOperate = canOperate;
                break;
            }
        }
        if (afterPutOutCardOperate == null) {
            afterPutOutCardOperate = new AfterPutOutCardOperate();
            afterPutOutCardOperate.setRoomMember(personalCardInfo.getRoomMember());
            afterPutOutCardOperate.setOperates(new HashSet<BaseOperate>());
            canOperates.add(afterPutOutCardOperate);
        }
        return afterPutOutCardOperate.getOperates();
    }

    /**
     * 找出手牌中拥有的宝牌
     */
    protected List<Mahjong> getMyBaoMahjongs(Set<Mahjong> handCards) {
        List<Mahjong> baoMahjongs = this.mahjongGameData.getBaoMahjongs();
        List<Mahjong> myBaoMahjongs = new ArrayList<>(4);

        if (baoMahjongs != null) {
            for (Mahjong baoMahjong : baoMahjongs) {
                if (handCards.contains(baoMahjong)) {
                    myBaoMahjongs.add(baoMahjong);
                }
            }
        }

        return myBaoMahjongs;
    }

    /**
     * 按麻将字号分组
     */
    protected Map<Integer, Set<Mahjong>> groupByZiHao(Set<Mahjong> handCards) {
        Map<Integer, Set<Mahjong>> Mahjongs = new HashMap<>(6);
        for (Mahjong handCard : handCards) {
            Integer ziHao = handCard.getZi();
            Set<Mahjong> ziHaoMahjongs = Mahjongs.get(ziHao);
            if (ziHaoMahjongs == null) {
                ziHaoMahjongs = new HashSet<>(19);//ceil(14/0.75)
                Mahjongs.put(ziHao, ziHaoMahjongs);
            }
            ziHaoMahjongs.add(handCard);
        }
        return Mahjongs;
    }

    /**
     * 按麻将号码分组
     */
    protected Map<Integer, Set<Mahjong>> groupByNumber(Set<Mahjong> mahjongs) {
        Map<Integer, Set<Mahjong>> sameNumberMahjongs = new HashMap<>(6);
        for (Mahjong handCard : mahjongs) {
            Integer number = handCard.getNumber();
            Set<Mahjong> numberMahjongs = sameNumberMahjongs.get(number);
            if (numberMahjongs == null) {
                numberMahjongs = new HashSet<>(6);//ceil(14/0.75)
                sameNumberMahjongs.put(number, numberMahjongs);
            }
            numberMahjongs.add(handCard);
        }
        return sameNumberMahjongs;
    }

}
