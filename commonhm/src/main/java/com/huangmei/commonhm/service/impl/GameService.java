package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.manager.putOutCard.AfterPutOutCardManager;
import com.huangmei.commonhm.manager.putOutCard.AfterPutOutCardOperate;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.VersionRedis;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

    Logger log = LoggerFactory.getLogger(GameService.class);

    @Autowired
    private GameRedis gameRedis;

    @Autowired
    private VersionRedis versionRedis;

    @Autowired
    private AfterPutOutCardManager afterPutOutCardManager;

    /***
     * 初始化数据包括：骰子的点数、每个人的手牌、剩余的牌等信息。
     * 一局游戏开始时，生成麻将的初始数据。
     */
    public Map<String, Object> firstPutOutCard(
            Room room, List<RoomMember> roomMembers) {

        // 对roomMembers按座位号升序
        Collections.sort(roomMembers, new Comparator<RoomMember>() {
            @Override
            public int compare(RoomMember o1, RoomMember o2) {
                return o1.getSeat() - o2.getSeat();
            }
        });

        Map<String, Object> result = new HashMap<>(6);

        int players = room.getPlayers();
        int bankerSite = 1;

        // 初始化一局麻将的数据
        MahjongGameData mahjongGameData = MahjongGameData.initData(players, bankerSite);
        log.debug("初始化一局麻将的数据:{}", JsonUtil.toJson(mahjongGameData));


        // 获取新版本号
        Long version = versionRedis.nextVersion(room.getId());
        mahjongGameData.setVersion(version);

        // 拆分成4份手牌数据，传给客户端
        List<MahjongGameData> singlePlayerGameDatas = new ArrayList<>(players);
        for (int i = 0; i < players; i++) {
            MahjongGameData singlePlayerGameData = new MahjongGameData();
            singlePlayerGameDatas.add(singlePlayerGameData);

            singlePlayerGameData.setBankerSite(mahjongGameData.getBankerSite());
            singlePlayerGameData.setDices(mahjongGameData.getDices());
            singlePlayerGameData.setLeftCardCount(mahjongGameData.getLeftCardCount());
            singlePlayerGameData.setOutCards(mahjongGameData.getOutCards());
            singlePlayerGameData.setVersion(version);

            // 添加玩家信息RoomMember
            List<PersonalCardInfo> personalCardInfos = new ArrayList<>(1);
            PersonalCardInfo personalCardInfo = mahjongGameData.getPersonalCardInfos().get(i);
            personalCardInfo.setRoomMember(roomMembers.get(i));
            personalCardInfos.add(personalCardInfo);
            singlePlayerGameData.setPersonalCardInfos(personalCardInfos);

        }
        result.put("playerGameData", singlePlayerGameDatas);

        log.debug("初始化一局麻将添加玩家信息RoomMember的数据:{}", JsonUtil.toJson(mahjongGameData));

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return result;

    }

    /**
     * 客户端打出一张牌的处理逻辑。
     * 遍历其他3个玩家，判断是否有碰，明杠，吃胡。
     * 如果有，则按优先级降序，存进redis的
     * 选择优先级最高的操作，返回给对应的客户端，等待其选择执行操作或着选择过
     * 如果选择过，则读redis待操作集合，循环上一步操作。
     * 如果全部人都选择过（待操作集合为空），则进入下家摸牌方法。
     * 如果待操作集合中有人选择了执行操作，则清空待操作集合，执行相应操作。
     *
     * @param putOutMahjong 打出的牌对象
     * @param user          用户信息
     * @param room          用户所在的房间
     * @param version       消息版本号
     */
    public void putOutCard(Mahjong putOutMahjong, Room room, User user,
                           Long version)
            throws InstantiationException, IllegalAccessException {

        Long nowVersion = versionRedis.nowVersion(room.getId());
        //Long nowVersion = 10l;
        if (!nowVersion.equals(version)) {
            throw CommonError.SYS_VERSION_TIMEOUT.newException();
        }

        // 取出麻将数据
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(
                room.getId());

        // 出牌验证
        if (!putOutCardValidate(putOutMahjong, mahjongGameData, user, room)) {
            throw CommonError.USER_NOT_HAVE_SPECIFIED_CARD.newException();
        }

        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            log.debug("验证后座位{}的手牌：{}{}",
                    personalCardInfo.getRoomMember().getSeat(),
                    personalCardInfo.getHandCards().size(),
                    personalCardInfo.getHandCards());
        }

        // 扫描其他用户是否有吃胡、大明杠、碰的操作
        ArrayList<AfterPutOutCardOperate> canOperates =
                afterPutOutCardManager.scan(mahjongGameData, putOutMahjong, user);
        log.debug("扫描出来可以的操作：{}", canOperates);


    }

    /**
     * 验证客户端出的牌是否合法，合法则在玩家的PersonalCardInfo手牌集合中移除该麻将牌
     */
    private boolean putOutCardValidate(Mahjong putOutCard, MahjongGameData
            mahjongGameData, User user, Room room) {
        // 获取玩家手牌信息
        PersonalCardInfo personalCardInfo = null;
        for (PersonalCardInfo gameData : mahjongGameData.getPersonalCardInfos()) {
            if (gameData.getRoomMember().getUserId().equals(user.getId())) {
                personalCardInfo = gameData;
                break;
            }
        }
        if (personalCardInfo == null) {
            throw CommonError.REDIS_GAME_DATA_ERROR.newException();
        }

        // 判断客户端打出的牌是不是刚摸上手的
        if (putOutCard == personalCardInfo.getTouchMahjong()) {
            personalCardInfo.setTouchMahjong(null);
            return true;
        }

        // 判断客户端打出的牌是不是其拥有的手牌
        log.debug("验证前座位{}的手牌：{}", personalCardInfo.getRoomMember().getSeat(),
                personalCardInfo.getHandCards());
        Iterator<Mahjong> iterator = personalCardInfo.getHandCards().iterator();
        while (iterator.hasNext()) {
            Mahjong mahjong = iterator.next();
            if (mahjong == putOutCard) {
                iterator.remove();
                return true;
            }
        }

        return false;

    }


}
