package com.huangmei.commonhm.dao;

import com.huangmei.commonhm.model.RoomMember;

import java.util.List;

public interface RoomMemberDao extends BaseDao<Integer, RoomMember> {
    RoomMember selectByUserIdForReady(RoomMember roomMember);
    List<RoomMember> selectForStart(RoomMember roomMember);
}