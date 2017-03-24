package com.huangmei.commonhm.dao.impl;

import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.model.RoomMember;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoomMemberDaoImpl extends BaseDaoImpl<Integer, RoomMember> implements RoomMemberDao {

    @Override
    public RoomMember selectByUserIdForCheck(RoomMember roomMember) {
        return sqlSessionTemplate.selectOne(
                statement("selectByUserIdForCheck")
        );
    }

    @Override
    public RoomMember selectByUserIdForReady(RoomMember roomMember) {

        return sqlSessionTemplate.selectOne(
                statement("selectByUserIdForReady")
        );
    }

    @Override
    public List<RoomMember> selectForStart(RoomMember roomMember) {
        return sqlSessionTemplate.selectList(
                statement("selectForStart"),
                roomMember
        );
    }
}