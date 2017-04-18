package com.huangmei.commonhm.service;

import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.User;
import net.sf.json.JSONObject;

import java.util.Map;

public interface RoomService extends BaseService<Integer, Room> {
    //public Map<String,Object> createRoom(JSONObject data,User user);
    Map<String, Object> createRoom(JSONObject data);

    Map<String, Object> joinRoom(JSONObject data, User user);

    Map<String, Object> outRoom(JSONObject data, User user);

    Map<String, Object> ready(User user) throws IllegalAccessException, InstantiationException;

    Map<String, Object> dismissRoom(JSONObject data, User user);

    Map<String, Object> agreeDismiss(JSONObject data, User user);

    Map<String, Object> numberOfPlayers(JSONObject data);

    Map<String, Object> communication(JSONObject data, User user);

    void dismissRoomVoteTask(Integer roomId);

    Map<String, Object> outRoom(Integer roomCode, Integer userId);

    void outRoom(Room room);

    Map<String, Object> roomInfo(User user);
}