package com.huangmei.commonhm.dao;

import com.huangmei.commonhm.model.Room;

public interface RoomDao extends BaseDao<Integer, Room> {
    long countForPlayers(Integer multiple);
}