package com.huangmei.commonhm.model.mahjong;

import com.huangmei.commonhm.model.RoomMember;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 玩家打出的牌
 */
public class OutCard {
    /**
     * 打出的牌的麻将对象
     */
    private Mahjong mahjong;

    /**
     * 打出这张牌的人
     */
    private RoomMember roomMember;

    public OutCard() {

    }

    public OutCard(Mahjong mahjong, RoomMember roomMember) {

        this.mahjong = mahjong;
        this.roomMember = roomMember;
    }

    /**
     * 去掉outMahjongs列表中包含mahjongs的麻将
     */
    public static void filterOutCard(List<OutCard> outMahjongs, List<Mahjong> mahjongs) {
        for (Mahjong mahjong : mahjongs) {
            Iterator<OutCard> iterator = outMahjongs.iterator();
            while (iterator.hasNext()) {
                OutCard next = iterator.next();
                if (next.getMahjong() == mahjong) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * 过滤掉非userId的麻将，返回新集合。原集合元素不变动
     */
    public static List<Integer> filterByUserId(List<OutCard> outMahjongs, Integer userId) {
        List<Integer> myMahjongIds = new ArrayList<>();
        for (OutCard outMahjong : outMahjongs) {
            if (outMahjong.getRoomMember().getUserId().equals(userId)) {
                myMahjongIds.add(outMahjong.getMahjong().getId());
            }
        }
        return myMahjongIds;
    }

    public Mahjong getMahjong() {
        return mahjong;
    }

    public void setMahjong(Mahjong mahjong) {
        this.mahjong = mahjong;
    }

    public RoomMember getRoomMember() {
        return roomMember;
    }

    public void setRoomMember(RoomMember roomMember) {
        this.roomMember = roomMember;
    }
}
