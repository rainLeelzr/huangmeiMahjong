package com.huangmei.commonhm.service;

import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.User;
import net.sf.json.JSONObject;

import java.util.Map;

public interface RoomService extends BaseService<Integer, Room> {
    public Map<String,Object> createRoom(JSONObject data,User user);

    Map<String,Object> joinRoom(JSONObject data,User user);

    Map<String,Object> outRoom(JSONObject data,User user);

    Map<String, Object> ready(JSONObject data,User user) throws IllegalAccessException, InstantiationException;


    Map<String, Object> dismissRoom(JSONObject data,User user);

    Map<String, Object> agreeDismiss(JSONObject data,User user);
}