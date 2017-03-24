package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.dao.UserDao;
import com.huangmei.commonhm.model.Entity;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.service.UserService;


import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.CommonUtil;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends BaseServiceImpl<Integer, User> implements UserService {
    @Autowired
    private UserDao dao;
    @Autowired
    private RoomMemberDao roomMemberDao;

    public Map<String, Object> login(JSONObject data, String ip) throws Exception {

        Map<String, Object> result = new HashMap<String, Object>(2);
        int login_type;//1正常登陆,2游戏中断线重连,3结算后未显示结算页面

        String openId = (String) data.get("openId");
        String nickName = (String) data.get("nickName");
        String image = (String) data.get("image");
        Integer sex = data.getInt("sex");

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setOpenId(Entity.Value.eq(openId));
        User user = dao.selectOne(userCriteria);


        if (user == null) {//新用户登录
            user = new User();
            user.setImage(image);
            user.setIp(ip);
            user.setLastLoginTime(new Date());
            user.setNickName(nickName);
            user.setSex(sex);
            user.setOpenId(openId);
            user.setCoin(0);
            user.setDiamond(0);
            user.setHorn(0);
            Integer uId = CommonUtil.createUserCode();
            user.setUId(uId);
            dao.save(user);
            login_type = 1;
        } else {
            if (!image.equals(user.getImage())) {//头像发生变化
                user.setImage(image);
            } else if (!nickName.equals(user.getNickName())) {//昵称发生变化
                user.setNickName(nickName);
            }
            user.setLastLoginTime(new Date());
            dao.update(user);

            RoomMember roomMember = new RoomMember();
            roomMember.setUserId(user.getId());
            roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
            roomMember= roomMemberDao.selectByUserIdForCheck(roomMember);
            if (roomMember!=null) {
                login_type = 2;
            } else {
                login_type = 1;
            }
        }
        result.put("user", user);
        result.put("login_type", login_type);
        return result;
    }


    public TextMessage TestConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User logout(JSONObject data) {
        Integer uId = data.getInt("uId");
        Entity.UserCriteria userCriteria=new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = dao.selectOne(userCriteria);
        if (user!=null){
            return user;
        }else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }

//	@Autowired
//	private UserDao dao;
//	/**
//	 * @object 请求体信息
//	 */
//	public JsonResult login(JSONObject object, String ip) throws Exception {
//		String open_id = (String)object.get("open_id");
//		String nick_name =(String) object.get("nike_name");
//		String image = (String)object.get("image");
//		Integer sex = object.getInt("sex");
//		
//		//根据唯一id查询信息
//		UserCriteria criteria = new UserCriteria();
//		criteria.setOpenId(Entity.Value.eq(open_id));
//		User user = selectOne(criteria);
//		
//		JsonResult jsonResult = new JsonResult();
//		JSONObject data = new JSONObject();
//		
//		if (user != null) {
//			// 用户名有没有变化
//			if (!nick_name.equals(user.getNickName())) {
//				user.setNickName(nick_name);
//			} else if (!image.equals(user.getImage())) {
//				user.setImage(image);
//			}
//			user.setLastLoginTime(new Date());
//			update(user);
//			data.put("uid", user.getUId());// 游戏id
//			data.put("name", user.getNickName());
//			data.put("cards", user.getCards());
//			data.put("ip", ip);
//			data.put("image", image);
//			data.put("sex", sex);
//			data.put("code", StatusUtil.SYS_SUCCESS);
//			jsonResult.setPid(PidValue.LOGIN);
//			jsonResult.setData(data);
//		} else {// 新用户登录
//			// 初始化新用户数据
//			user = new User();
//			user.setOpenId(open_id);
//			user.setCards(0);
//			user.setNickName(nick_name);
//			user.setLastLoginTime(new Date());
//			user.setImage(image);
//			user.setIp(ip);
//			user.setSex(sex);
//			Integer u_id = CommonUtil.createUserCode(); //随机获取一位6位的数字，作为用户的唯一标识
//			while(hasUserCode(u_id)){
//				u_id = CommonUtil.createUserCode();
//			}
//			user.setUId(u_id);
//			save(user);
//			// 封装json
//			data.put("uid", user.getId());
//			data.put("cards", user.getCards());
//			data.put("name", user.getNickName());
//			data.put("image", image);
//			data.put("sex", sex);
//			data.put("ip", ip);
//			jsonResult.setPid(PidValue.LOGIN);
//			jsonResult.setError(StatusUtil.SYS_SUCCESS);
//			jsonResult.setData(data);
//		}
//		return jsonResult;
//	}
//	/**
//	 * 获取个人信息 
//	 */
//	public TextMessage getUser(JSONObject data, WebSocketSession session) {
//		JsonResult jsonResult = new JsonResult();
//		JSONObject jsonObject = new JSONObject();
//		//获取客户端传来的数据
//		Integer u_id=(Integer)data.get("uid");
//		//根据user_code查询个人信息
//		UserCriteria criteria = new UserCriteria();
//		criteria.setUId(Entity.Value.eq(u_id));
//		User user = selectOne(criteria);
//		if(user!=null){
//			//封装json对象
//			jsonObject.put("uid", user.getUId());
//			jsonObject.put("ip", user.getIp());			
//			jsonObject.put("name", user.getNickName());
//			jsonObject.put("image", user.getImage());
//			jsonObject.put("cards", user.getCards());
//			jsonResult.setPid(PidValue.GET_USER);
//			jsonResult.setError(StatusUtil.SYS_SUCCESS);
//			jsonResult.setData(jsonObject);
//		}else{
//			jsonResult.setPid(PidValue.GET_USER);
//			jsonResult.setError(StatusUtil.USER_ILLEGAL);
//		}
//		TextMessage textMessage = new TextMessage(jsonResult.toString());
//		return textMessage;
//	}
//
//	public TextMessage TestConnection() {
//		JsonResult jsonResult=new JsonResult();
//		jsonResult.setError(StatusUtil.SYS_SUCCESS);
//		jsonResult.setPid(PidValue.CHECK_CLIENT);
//		TextMessage textMessage = new TextMessage(jsonResult.toString());
//		return textMessage;
//	}
//	/**判断userCode是否已存在*/
//	public boolean hasUserCode(Integer u_id){
//		UserCriteria criteria = new UserCriteria();
//		criteria.setUId(Value.eq(u_id));
//		User c = selectOne(criteria);
//		return null != c;
//	}

}