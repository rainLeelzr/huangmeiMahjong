package com.huangmei.commonhm.service;

import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import net.sf.json.JSONObject;

import java.util.Map;

public interface RoomService extends BaseService<Integer, Room> {
    public Map<String,Object> createRoom(JSONObject data);

    Map<String,Object> joinRoom(JSONObject data);

    Map<String,Object> outRoom(JSONObject data);

    RoomMember ready(JSONObject data);

    RoomMember startGame(JSONObject data);
}