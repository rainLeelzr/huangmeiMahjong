package com.huangmei.commonhm.dao;

import com.huangmei.commonhm.model.Entity;
import com.huangmei.commonhm.model.RoomMember;

import java.util.List;

public interface RoomMemberDao extends BaseDao<Integer, RoomMember> {
    RoomMember selectByUserIdForCheck(RoomMember roomMember);
    RoomMember selectByUserIdForReady(RoomMember roomMember);
    List<RoomMember> selectForStart(RoomMember roomMember);
    List<RoomMember> selectForDismiss(RoomMember roomMember);
}