package com.huangmei.commonhm.redis;

import com.huangmei.commonhm.manager.operate.CanDoOperate;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import com.huangmei.commonhm.redis.base.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.TreeSet;

@Component
public class GameRedis {

    private static String MAHJONG_GAME_DATA_FIELD_KEY = "mahjongGameData";

    private static String WAITING_CLIENT_OPERATE_FIELD_KEY = "waitingClientOperate";

    private static String CLIENT_OPERATE_QUEUE_SET_KEY = "clientOperateQueue_roomId_%s";

    @Autowired
    private Redis redis;

    public void saveWaitingClientOperate(CanDoOperate canDoOperate) {
        redis.hash.put(
                String.format(RoomRedis.ROOM_KEY, canDoOperate.getRoomMember().getRoomId()),
                WAITING_CLIENT_OPERATE_FIELD_KEY,
                canDoOperate);
    }

    public void saveMahjongGameData(MahjongGameData mahjongGameData) {
        redis.hash.put(
                String.format(RoomRedis.ROOM_KEY, mahjongGameData.getRoomId()),
                MAHJONG_GAME_DATA_FIELD_KEY,
                mahjongGameData);
    }

    public MahjongGameData getMahjongGameData(Integer roomId) {
        MahjongGameData temp = (MahjongGameData) redis.hash.get(
                String.format(RoomRedis.ROOM_KEY, roomId),
                MAHJONG_GAME_DATA_FIELD_KEY,
                MahjongGameData.class,
                MahjongGameData.classMap);
        if (temp != null && temp.getPersonalCardInfos() != null && temp
                .getPersonalCardInfos().size() != 0) {
            for (PersonalCardInfo personalCardInfo : temp
                    .getPersonalCardInfos()) {
                personalCardInfo.setHandCards(
                        new TreeSet<>(personalCardInfo.getHandCards())
                );
            }
        }
        return temp;
    }

    public CanDoOperate getWaitingClientOperate(Integer roomId) {
        CanDoOperate temp = (CanDoOperate) redis.hash.get(
                String.format(RoomRedis.ROOM_KEY, roomId),
                WAITING_CLIENT_OPERATE_FIELD_KEY,
                CanDoOperate.class,
                CanDoOperate.classMap);
        return temp;
    }

    public void deleteWaitingClientOperate(Integer roomId) {
        redis.hash.delete(String.format(RoomRedis.ROOM_KEY, roomId),
                WAITING_CLIENT_OPERATE_FIELD_KEY);
    }

    public void saveCanOperates(ArrayList<CanDoOperate> canOperates) {
        for (CanDoOperate canOperate : canOperates) {
            redis.sortedSet.add(
                    String.format(CLIENT_OPERATE_QUEUE_SET_KEY, canOperate.getRoomMember().getRoomId()),
                    canOperate,
                    canOperate.getRoomMember().getSeat()
            );
        }
    }

    public void getNextCanOperates(Integer roomId) {
        CanDoOperate canDoOperate = (CanDoOperate) redis.sortedSet.getByMinScore(
                String.format(CLIENT_OPERATE_QUEUE_SET_KEY, roomId), CanDoOperate.class);

    }
}
