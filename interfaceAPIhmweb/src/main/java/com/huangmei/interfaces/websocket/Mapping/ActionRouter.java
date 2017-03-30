package com.huangmei.interfaces.websocket.Mapping;

import com.huangmei.commonhm.manager.getACard.GetACardManager;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.FirstPutOutCard;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.base.Redis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.service.UserService;
import com.huangmei.commonhm.service.impl.GameService;
import com.huangmei.commonhm.util.*;
import com.huangmei.interfaces.monitor.MonitorManager;
import com.huangmei.interfaces.monitor.clientTouchMahjong.task.ClientTouchMahjongTask;
import com.huangmei.interfaces.websocket.MessageManager;
import com.huangmei.interfaces.websocket.SessionManager;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;

/**
 * 客户端接入到此类的某一个方法
 */
@Component
public class ActionRouter {

    Logger log = LoggerFactory.getLogger(ActionRouter.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private GameService gameService;

    @Autowired
    private Redis redis;

    @Autowired
    private MonitorManager monitorManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private GetACardManager getACardManager;

    @Autowired
    private GameRedis gameRedis;

    @Pid(PidValue.LOGIN)
    public JsonResultY login(WebSocketSession session, JSONObject data)
            throws Exception {

        // 获取微信的唯一标识id
        // 服务端验证微信用户
        String ip = IpAddressUtil.getIp(session);
        //String openId = (String) data.get("openId");
        Map<String, Object> result = userService.login(data, ip);
        //登录成功时，将此user对应的session缓存起来
        if (result != null) {
            sessionManager.userLogin((User) result.get("user"), session);
        }

        return new JsonResultY.Builder()
                .setPid(PidValue.LOGIN.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.LOGOUT)
    public JsonResultY logout(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = userService.logout(data);
        sessionManager.userLogout(user, session);

        return new JsonResultY.Builder()
                .setPid(PidValue.LOGOUT.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(null)
                .build();
    }

    @Pid(PidValue.GET_USER)
    @LoginResource
    public JsonResultY getUserInfo(WebSocketSession session, JSONObject data)
            throws Exception {

        //String userId = (String) data.get("userId");
        //User user = userService.selectOne(Integer.parseInt(userId));
        //if (user == null) {
        //    throw  CommonError.USER_NOT_EXIST.newException();
        //}
        User user = sessionManager.getUser(session.getId());
        //String roomId = "11";
        //String version = "1111";
        //monitorManager.watch(new PengCardMonitorTask
        //        .Builder()
        //        .setRoomId(roomId)
        //        .setUserId(userId)
        //        .setVersion(version)
        //        .build());
        return new JsonResultY.Builder()
                .setPid(PidValue.GET_USER.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(user)
                .build();
    }

    @Pid(PidValue.CREATE_ROOM)
    @LoginResource
    public JsonResultY createRoom(WebSocketSession session, JSONObject data)
            throws Exception {


        Map<String, Object> result = roomService.createRoom(data);
        if (result != null) {
            sessionManager.userJoinRoom((Room) result.get(("room")), session);
        }

        return new JsonResultY.Builder()
                .setPid(PidValue.CREATE_ROOM.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.JOIN_ROOM)
    @LoginResource
    public JsonResultY joinRoom(WebSocketSession session, JSONObject data)
            throws Exception {
        Map<String, Object> result = roomService.joinRoom(data);
        if (result != null) {
            sessionManager.userJoinRoom((Room) result.get(("room")), session);
        }
        Map<String, Object> myResult = new HashMap<>();
        Set<RoomMember> roomMembers = (Set<RoomMember>) result.get(("roomMembers"));
        for (RoomMember roomMember : roomMembers) {
            if (roomMember.getUserId().equals((Integer) result.get("userId"))) {
                myResult.put("roomMember", roomMember);
            }

        }
        List<User> users = (ArrayList<User>) result.get(("users"));
        for (User user : users) {
            if (user.getId().equals((Integer) result.get("userId"))) {
                myResult.put("user", user);
            }

        }
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.JOIN_ROOM_MESSAGE.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(myResult)
                .build();

        messageManager.sendMessageToOtherRoomUsers(
                ((Room) result.get(("room"))).getId().toString(),
                (Integer) result.get("userId"),
                jsonResultY);

        result.remove("userId");
        return new JsonResultY.Builder()
                .setPid(PidValue.JOIN_ROOM.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.OUT_ROOM)
    @LoginResource
    public JsonResultY outRoom(WebSocketSession session, JSONObject data)
            throws Exception {
        Map<String, Object> result = roomService.outRoom(data);
        if (result != null) {
            JsonResultY jsonResultY = new JsonResultY.Builder()
                    .setPid(PidValue.OUT_ROOM.getPid())
                    .setError(CommonError.SYS_SUSSES)
                    .setData(result)
                    .build();
            messageManager.sendMessageToRoomUsers(((Room) result.get(("room"))).getId().toString(), jsonResultY);
            sessionManager.userExitRoom((RoomMember) result.get("roomMember"), session);
        }


        return null;
    }

    @SuppressWarnings("unchecked")
    @Pid(PidValue.READY)
    @LoginResource
    public JsonResultY ready(WebSocketSession session, JSONObject data)
            throws Exception {
        Map<String, Object> result = roomService.ready(data);
        Integer type = (Integer) result.get("type");

        if (type == 2) {
            List<FirstPutOutCard> firstPutOutCards =
                    (List<FirstPutOutCard>) result.get(GameService.FIRST_PUT_OUT_CARD_KEY);

            MahjongGameData mahjongGameData = (MahjongGameData) result.get(
                    MahjongGameData.class.getSimpleName());
            result.remove(MahjongGameData.class.getSimpleName());

            // 获取庄家uId
            Integer bankerUserId = firstPutOutCards.get(0).getBankerUId();
            WebSocketSession bankerSession = sessionManager.getByUserId(bankerUserId);
            User bankerUser;
            if (bankerSession == null) {
                bankerUser = userService.selectOne(bankerUserId);
            } else {
                bankerUser = sessionManager.getUser(bankerSession.getId());
            }

            // 4个玩家，按座位号升序
            List<User> users = new ArrayList<>(firstPutOutCards.size());

            // 广播给4个用户第一次发牌
            for (FirstPutOutCard firstPutOutCard : firstPutOutCards) {
                Map<String, Object> myResult = new HashMap<>();
                //myResult.put("type", 2);

                // 获取庄家uId
                firstPutOutCard.setBankerUId(bankerUser.getUId());

                // 需要接受广播消息的用户uid
                Integer acceptBroadcastUserId = firstPutOutCard.getuId();
                User acceptBroadcastUser;
                if (acceptBroadcastUserId.equals(bankerUserId)) {
                    acceptBroadcastUser = bankerUser;
                } else {
                    WebSocketSession tempSession = sessionManager.getByUserId(acceptBroadcastUserId);
                    if (tempSession == null) {
                        acceptBroadcastUser = userService.selectOne(acceptBroadcastUserId);
                    } else {
                        acceptBroadcastUser = sessionManager.getUser(tempSession.getId());
                    }
                }
                firstPutOutCard.setuId(acceptBroadcastUser.getUId());

                users.add(acceptBroadcastUser);

                myResult.put(GameService.FIRST_PUT_OUT_CARD_KEY, firstPutOutCard);
                JsonResultY temp = new JsonResultY.Builder()
                        .setPid(PidValue.FIRST_PUT_OUT_ALL_CARD.getPid())
                        .setError(CommonError.SYS_SUSSES)
                        .setData(myResult)
                        .build();
                messageManager.sendMessageByUserId(acceptBroadcastUserId, temp);
            }

            monitorManager.watch(new ClientTouchMahjongTask
                    .Builder()
                    .setGetACardManager(getACardManager)
                    .setMessageManager(messageManager)
                    .setMahjongGameData(mahjongGameData)
                    .setUser(bankerUser)
                    .setUsers(users)
                    .setGameRedis(gameRedis)
                    .build());

        }
        result.remove("type");
        result.remove(GameService.FIRST_PUT_OUT_CARD_KEY);
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.READY.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
        messageManager.sendMessageToRoomUsers(
                (result.get("roomId")).toString(),
                jsonResultY);

        return null;
    }

    @Pid(PidValue.DISMISS_ROOM)
    @LoginResource
    public JsonResultY dismissRoom(WebSocketSession session, JSONObject data)
            throws Exception {
        Map<String, Object> result = roomService.dismissRoom(data);

        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.DISMISS_ROOM.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
        messageManager.sendMessageToRoomUsers(
                ((Room) (result.get("room"))).getId().toString(),
                jsonResultY);

        return null;
    }

    @Pid(PidValue.AGREE_DISMISS)
    @LoginResource
    public JsonResultY agreeDismiss(WebSocketSession session, JSONObject data)
            throws Exception {
        Map<String, Object> result = roomService.agreeDismiss(data);
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.AGREE_DISMISS.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
        messageManager.sendMessageToRoomUsers(
                ((Room) (result.get("room"))).getId().toString(),
                jsonResultY);
        return null;
    }

    @Pid(PidValue.TEST)
    public JsonResultY test(WebSocketSession session, JSONObject data)
            throws Exception {
        // 测试数据
        Room room = new Room();
        room.setId(222);
        room.setPlayers(4);

        RoomMember[] roomMembers = new RoomMember[room.getPlayers()];
        for (int i = 0; i < room.getPlayers(); i++) {
            roomMembers[i] = new RoomMember();
            roomMembers[i].setId(i + 1);
            roomMembers[i].setUserId(i + 1);
            roomMembers[i].setJoinTime(new Date());
            roomMembers[i].setRoomId(room.getId());
            roomMembers[i].setSeat(i + 1);
            roomMembers[i].setState(RoomMember.state.UNREADY.getCode());
        }

        // 初始化游戏数据
        //Map<String, Object> mahjongGameDatas = gameService.firstPutOutCard
        //        (room, roomMembers);
        //JsonUtil.toJson(mahjongGameDatas);

        // version


        return new JsonResultY.Builder()
                .setPid(PidValue.TEST)
                .setError(CommonError.SYS_SUSSES)
                .setData(null)
                .build();
    }

    @Pid(PidValue.PLAY_A_MAHJONG)
    @LoginResource
    public JsonResultY playACard(WebSocketSession session, JSONObject data)
            throws Exception {

        int mahjongId = JsonUtil.getInt(data, "mahjongId");
        long version = JsonUtil.getLong(data, "version");

        Mahjong playedMahjong = Mahjong.parse(mahjongId);

        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        gameService.playAMahjong(room, user, playedMahjong, version);
        //gameService.putOutCard(putOutCard, room, user, version);

        return new JsonResultY.Builder()
                .setPid(PidValue.PLAY_A_MAHJONG)
                .setError(CommonError.SYS_SUSSES)
                .setData(null)
                .build();
    }


}
