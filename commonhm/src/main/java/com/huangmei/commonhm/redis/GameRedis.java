package com.huangmei.commonhm.redis;

import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import com.huangmei.commonhm.redis.base.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.TreeSet;

@Component
public class GameRedis {

    private static String MAHJONG_GAME_DATA_FIELD_KEY = "mahjongGameData";

    @Autowired
    private Redis redis;

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
        if (temp != null || temp.getPersonalCardInfos() != null || temp
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
}
