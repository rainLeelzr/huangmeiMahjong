package com.huangmei.commomhm.service;

import com.huangmei.AbstractTestClass;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.VersionRedis;
import com.huangmei.commonhm.service.impl.GameService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Set;


public class GameServiceTest extends AbstractTestClass {

    public static final Long mockVersion = 10l;

    private static final Logger log = LoggerFactory.getLogger
            (AbstractTestClass.class);

    @Autowired
    GameService gameService;

    @Autowired
    VersionRedis versionRedis;

    @Autowired
    GameRedis gameRedis;

    @Test
    public void TestPutOutCard() throws IllegalAccessException, InstantiationException {
        // 测试数据
        Room room = new Room();
        room.setId(222);
        room.setPlayers(4);

        RoomMember[] roomMembers = new RoomMember[room.getPlayers()];
        for (int i = 0; i < room.getPlayers(); i++) {
            roomMembers[i] = new RoomMember();
            roomMembers[i].setId(i + 1);
            roomMembers[i].setUserId(i + 1);
            roomMembers[i].setJoinTime(new Date());
            roomMembers[i].setRoomId(room.getId());
            roomMembers[i].setSeat(i + 1);
            roomMembers[i].setState(RoomMember.state.UNREADY.getCode());
        }

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());



        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = gameService.mockMahjongGameData(room,roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);

    }



}
