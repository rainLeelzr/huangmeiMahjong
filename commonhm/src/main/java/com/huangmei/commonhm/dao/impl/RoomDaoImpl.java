package com.huangmei.commonhm.dao.impl;

import com.huangmei.commonhm.dao.RoomDao;
import com.huangmei.commonhm.model.Room;
import org.springframework.stereotype.Repository;

@Repository
public class RoomDaoImpl extends BaseDaoImpl<Integer, Room> implements RoomDao {

    @Override
    public long countForPlayers(Integer multiple) {

        return sqlSessionTemplate.selectOne(
                statement("countForPlayers"),
                multiple);
    }
}