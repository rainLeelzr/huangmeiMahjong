package com.huangmei.commonhm.service;

import net.sf.json.JSONObject;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.util.JsonResult;

import java.util.Map;


public interface UserService extends BaseService<Integer, User> {
	public Map<String, Object> login(JSONObject data, String ip) throws Exception;
	public TextMessage TestConnection() ;

	User logout(JSONObject data);
}