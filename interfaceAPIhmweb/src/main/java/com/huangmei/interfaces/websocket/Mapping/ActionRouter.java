package com.huangmei.interfaces.websocket.Mapping;

import com.huangmei.commonhm.manager.getACard.GetACardManager;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.*;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.VersionRedis;
import com.huangmei.commonhm.redis.base.Redis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.service.UserService;
import com.huangmei.commonhm.service.impl.GameService;
import com.huangmei.commonhm.util.*;
import com.huangmei.interfaces.monitor.MonitorManager;
import com.huangmei.interfaces.monitor.clientTouchMahjong.task.ClientTouchMahjongTask;
import com.huangmei.interfaces.monitor.clientTouchMahjong.toucher.CommonToucher;
import com.huangmei.interfaces.monitor.clientTouchMahjong.toucher.GangToucher;
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

    @Autowired
    private VersionRedis versionRedis;

    private User getUserByUserId(Integer userId) {
        WebSocketSession session = sessionManager.getByUserId(userId);
        User user;
        if (session == null) {
            user = userService.selectOne(userId);
        } else {
            user = sessionManager.getUser(session.getId());
        }
        return user;
    }

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

            Integer loginType = (Integer) result.get("login_type");
            if (loginType == 2) {
                sessionManager.userJoinRoom((Room) result.get(("room")), session);
                result.remove("room");
            }
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

        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = userService.getUser(data, user);
        //String userId = (String) data.get("userId");
        //User user = userService.selectOne(Integer.parseInt(userId));
        //if (user == null) {
        //    throw  CommonError.USER_NOT_EXIST.newException();
        //}
        //User user = sessionManager.getUser(session.getId());
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
                .setData(result)
                .build();
    }

    @Pid(PidValue.CREATE_ROOM)
    @LoginResource
    public JsonResultY createRoom(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        //Map<String, Object> result = roomService.createRoom(data,user);
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
        User user = sessionManager.getUser(session.getId());

        Map<String, Object> result = roomService.joinRoom(data, user);
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
        for (User u : users) {
            if (u.getId().equals((Integer) result.get("userId"))) {
                myResult.put("user", u);
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
        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = roomService.outRoom(data, user);
        if (result != null) {
            Integer roomId = (Integer) result.get("roomId");
            result.remove("roomId");
            JsonResultY jsonResultY = new JsonResultY.Builder()
                    .setPid(PidValue.OUT_ROOM.getPid())
                    .setError(CommonError.SYS_SUSSES)
                    .setData(result)
                    .build();
            messageManager.sendMessageToRoomUsers(roomId.toString(), jsonResultY);
            sessionManager.userExitRoom(roomId.toString(), session);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Pid(PidValue.READY)
    @LoginResource
    public JsonResultY ready(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = roomService.ready(data, user);
        Integer type = (Integer) result.get("type");

        boolean isFirstPutOutCard = false;
        List<Object[]> firstPutOutCardBroadcasts = new ArrayList<>(4);

        if (type == 2) {
            isFirstPutOutCard = true;

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
                myResult.put("type", 2);

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

                Object[] broadcast = new Object[]{
                        acceptBroadcastUserId, temp
                };
                firstPutOutCardBroadcasts.add(broadcast);
            }

            // 庄家摸一张牌
            monitorManager.watch(new ClientTouchMahjongTask
                    .Builder()
                    .setToucher(new CommonToucher())
                    .setGetACardManager(getACardManager)
                    .setMessageManager(messageManager)
                    .setMahjongGameData(mahjongGameData)
                    .setUser(bankerUser)
                    .setUsers(users)
                    .setGameRedis(gameRedis)
                    .setVersionRedis(versionRedis)
                    .build());

        }
        result.remove(GameService.FIRST_PUT_OUT_CARD_KEY);
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.READY.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
        messageManager.sendMessageToRoomUsers(
                (result.get("roomId")).toString(),
                jsonResultY);

        // 如果是第一次发牌，则广播給客户端他们各自的牌
        if (isFirstPutOutCard) {
            for (Object[] firstPutOutCardBroadcast : firstPutOutCardBroadcasts) {
                messageManager.sendMessageByUserId(
                        (Integer) firstPutOutCardBroadcast[0],
                        (JsonResultY) firstPutOutCardBroadcast[1]);
            }
        }

        return null;
    }

    @Pid(PidValue.DISMISS_ROOM)
    @LoginResource
    public JsonResultY dismissRoom(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = roomService.dismissRoom(data, user);
        Integer roomId = (Integer) result.get("roomId");
        result.remove("roomId");
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.DISMISS_ROOM.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
        messageManager.sendMessageToRoomUsers(
                roomId.toString(),
                jsonResultY);

        return null;
    }

    @Pid(PidValue.AGREE_DISMISS)
    @LoginResource
    public JsonResultY agreeDismiss(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = roomService.agreeDismiss(data, user);
        Integer roomId = (Integer) result.get("roomId");
        result.remove("roomId");
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.AGREE_DISMISS.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
        messageManager.sendMessageToRoomUsers(
                roomId.toString(),
                jsonResultY);
        return null;
    }

    @Pid(PidValue.PRIZE_DRAW)
    @LoginResource
    public JsonResultY prizeDraw(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());

        Map<String, Object> result = userService.prizeDraw(data, user);

        sessionManager.userUpdate((User) result.get("user"), session);

        return new JsonResultY.Builder()
                .setPid(PidValue.PRIZE_DRAW.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.FREE_COINS)
    @LoginResource
    public JsonResultY freeCoins(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());

        Map<String, Object> result = userService.freeCoins(data, user);

        sessionManager.userUpdate((User) result.get("user"), session);

        return new JsonResultY.Builder()
                .setPid(PidValue.FREE_COINS.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
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
        //     (room, roomMembers);
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
    @SuppressWarnings("unchecked")
    public JsonResultY playACard(WebSocketSession session, JSONObject data)
            throws Exception {

        int mahjongId = JsonUtil.getInt(data, "mahjongId");
        long version = JsonUtil.getLong(data, "version");

        Mahjong playedMahjong = Mahjong.parse(mahjongId);

        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        Map<String, Object> result = gameService.playAMahjong(room, user, playedMahjong, version);

        // 响应用户已经打出牌
        messageManager.send(session, new JsonResultY.Builder()
                .setPid(PidValue.PLAY_A_MAHJONG)
                .setError(CommonError.SYS_SUSSES)
                .setData(null)
                .build());

        // 玩家打牌广播
        List<PlayedMahjong> playedMahjongs = (List<PlayedMahjong>) result.get(PlayedMahjong.class.getSimpleName());
        for (PlayedMahjong mahjong : playedMahjongs) {
            int acceptUserId = mahjong.getuId();
            mahjong.setuId(getUserByUserId(acceptUserId).getUId());

            messageManager.sendMessageByUserId(acceptUserId, new JsonResultY.Builder()
                    .setPid(PidValue.OTHER_USER_PLAY_A_MAHJONG)
                    .setError(CommonError.SYS_SUSSES)
                    .setData(mahjong)
                    .build());
        }
        //gameService.putOutCard(putOutCard, room, user, version);

        MahjongGameData mahjongGameData = (MahjongGameData) result.get(MahjongGameData.class.getSimpleName());
        // 4个玩家，按座位号升序
        List<User> users = getRoomUsers(mahjongGameData.getPersonalCardInfos());

        // 一个玩家出牌后，轮到下一个玩家摸牌
        User nextUser = getNextTouchMahjongUser(mahjongGameData, user);

        // 下一个玩家摸一张牌
        monitorManager.watch(new ClientTouchMahjongTask
                .Builder()
                .setToucher(new CommonToucher())
                .setGetACardManager(getACardManager)
                .setMessageManager(messageManager)
                .setMahjongGameData(mahjongGameData)
                .setUser(nextUser)
                .setUsers(users)
                .setGameRedis(gameRedis)
                .setVersionRedis(versionRedis)
                .build());

        return null;
    }

    @Pid(PidValue.YING_AN_GANG)
    @LoginResource
    public JsonResultY yingAnGang(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        List<Integer> toBeGangMahjongIds = JsonUtil.getIntegerList(data, "mahjongIds");

        List<Mahjong> mahjongs = Mahjong.parseFromIds(toBeGangMahjongIds);
        if (mahjongs.size() != 4) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        MahjongGameData mahjongGameData = gameService.yingAnGang(user, room, mahjongs);

        // 响应玩家通过暗杠验证
        messageManager.send(
                session,
                new JsonResultY.Builder()
                        .setPid(PidValue.YING_AN_GANG)
                        .setError(CommonError.SYS_SUSSES)
                        .build());

        // 广播玩家执行暗杠
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            GangBroadcast anGangBroadcast =
                    new GangBroadcast(
                            toBeGangMahjongIds,
                            user.getUId(),
                            getUserByUserId(
                                    personalCardInfo.getRoomMember().getUserId()
                            ).getUId()
                    );
            messageManager.sendMessageByUserId(
                    personalCardInfo.getRoomMember().getUserId(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.GANG_BROADCAST)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(anGangBroadcast)
                            .build());
        }

        // 4个玩家，按座位号升序
        List<User> users = getRoomUsers(mahjongGameData.getPersonalCardInfos());

        // 玩家在leftCards的开头摸一张牌，并广播
        monitorManager.watch(new ClientTouchMahjongTask
                .Builder()
                .setToucher(new GangToucher())
                .setGetACardManager(getACardManager)
                .setMessageManager(messageManager)
                .setMahjongGameData(mahjongGameData)
                .setUser(user)
                .setUsers(users)
                .setGameRedis(gameRedis)
                .setVersionRedis(versionRedis)
                .build());

        return null;
    }

    @Pid(PidValue.RUAN_AN_GANG)
    @LoginResource
    public JsonResultY ruanAnGang(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        List<Integer> toBeGangMahjongIds = JsonUtil.getIntegerList(data, "mahjongIds");

        List<Mahjong> mahjongs = Mahjong.parseFromIds(toBeGangMahjongIds);
        if (mahjongs.size() != 4) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        MahjongGameData mahjongGameData = gameService.ruanAnGang(user, room, mahjongs);

        // 响应玩家通过暗杠验证
        messageManager.send(
                session,
                new JsonResultY.Builder()
                        .setPid(PidValue.RUAN_AN_GANG)
                        .setError(CommonError.SYS_SUSSES)
                        .build());

        // 广播玩家执行软暗杠
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            GangBroadcast anGangBroadcast =
                    new GangBroadcast(
                            toBeGangMahjongIds,
                            user.getUId(),
                            getUserByUserId(
                                    personalCardInfo.getRoomMember().getUserId()
                            ).getUId()
                    );
            messageManager.sendMessageByUserId(
                    personalCardInfo.getRoomMember().getUserId(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.GANG_BROADCAST)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(anGangBroadcast)
                            .build());
        }

        // 4个玩家，按座位号升序
        List<User> users = getRoomUsers(mahjongGameData.getPersonalCardInfos());

        // 玩家在leftCards的开头摸一张牌，并广播
        monitorManager.watch(new ClientTouchMahjongTask
                .Builder()
                .setToucher(new GangToucher())
                .setGetACardManager(getACardManager)
                .setMessageManager(messageManager)
                .setMahjongGameData(mahjongGameData)
                .setUser(user)
                .setUsers(users)
                .setGameRedis(gameRedis)
                .setVersionRedis(versionRedis)
                .build());

        return null;
    }

    @Pid(PidValue.YING_JIA_GANG)
    @LoginResource
    public JsonResultY yingJiaGang(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        Integer toBeJiaGangMahjongId = JsonUtil.getInt(data, "mahjongId");

        Mahjong mahjong = Mahjong.parse(toBeJiaGangMahjongId);

        Object[] result = gameService.yingJiaGang(user, room, mahjong);
        MahjongGameData mahjongGameData = (MahjongGameData) result[0];
        Combo jiaGangCombo = (Combo) result[1];

        // 响应玩家通过硬加杠验证
        messageManager.send(
                session,
                new JsonResultY.Builder()
                        .setPid(PidValue.YING_JIA_GANG)
                        .setError(CommonError.SYS_SUSSES)
                        .build());

        // 广播玩家执行硬加杠
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            GangBroadcast yingJiaGangBroadcast =
                    new GangBroadcast(
                            Mahjong.parseToIds(jiaGangCombo.mahjongs),
                            user.getUId(),
                            getUserByUserId(
                                    personalCardInfo.getRoomMember().getUserId()
                            ).getUId()
                    );
            messageManager.sendMessageByUserId(
                    personalCardInfo.getRoomMember().getUserId(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.GANG_BROADCAST)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(yingJiaGangBroadcast)
                            .build());
        }

        // 4个玩家，按座位号升序
        List<User> users = getRoomUsers(mahjongGameData.getPersonalCardInfos());

        // 玩家在leftCards的开头摸一张牌，并广播
        monitorManager.watch(new ClientTouchMahjongTask
                .Builder()
                .setToucher(new GangToucher())
                .setGetACardManager(getACardManager)
                .setMessageManager(messageManager)
                .setMahjongGameData(mahjongGameData)
                .setUser(user)
                .setUsers(users)
                .setGameRedis(gameRedis)
                .setVersionRedis(versionRedis)
                .build());

        return null;
    }

    @Pid(PidValue.RUAN_JIA_GANG)
    @LoginResource
    public JsonResultY ruanJiaGang(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        // 0,1,2下标是原有的碰麻将，3是加杠的麻将
        List<Integer> toBeRuanJiaGangMahjongId = JsonUtil.getIntegerList(data, "mahjongIds");
        if (toBeRuanJiaGangMahjongId.size() != 4) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }


        List<Mahjong> mahjongs = new ArrayList<>(toBeRuanJiaGangMahjongId.size());
        for (Integer mahjongId : toBeRuanJiaGangMahjongId) {
            mahjongs.add(Mahjong.parse(mahjongId));
        }

        Object[] result = gameService.ruanJiaGang(user, room, mahjongs);
        MahjongGameData mahjongGameData = (MahjongGameData) result[0];
        Combo jiaGangCombo = (Combo) result[1];

        // 响应玩家通过软加杠验证
        messageManager.send(
                session,
                new JsonResultY.Builder()
                        .setPid(PidValue.RUAN_JIA_GANG)
                        .setError(CommonError.SYS_SUSSES)
                        .build());

        // 广播玩家执行软加杠
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            GangBroadcast ruanJiaGangBroadcast =
                    new GangBroadcast(
                            Mahjong.parseToIds(jiaGangCombo.mahjongs),
                            user.getUId(),
                            getUserByUserId(
                                    personalCardInfo.getRoomMember().getUserId()
                            ).getUId()
                    );
            messageManager.sendMessageByUserId(
                    personalCardInfo.getRoomMember().getUserId(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.GANG_BROADCAST)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(ruanJiaGangBroadcast)
                            .build());
        }

        // 4个玩家，按座位号升序
        List<User> users = getRoomUsers(mahjongGameData.getPersonalCardInfos());

        // 玩家在leftCards的开头摸一张牌，并广播
        monitorManager.watch(new ClientTouchMahjongTask
                .Builder()
                .setToucher(new GangToucher())
                .setGetACardManager(getACardManager)
                .setMessageManager(messageManager)
                .setMahjongGameData(mahjongGameData)
                .setUser(user)
                .setUsers(users)
                .setGameRedis(gameRedis)
                .setVersionRedis(versionRedis)
                .build());

        return null;
    }

    private List<User> getRoomUsers(List<PersonalCardInfo> personalCardInfos) {
        List<User> users = new ArrayList<>(personalCardInfos.size());
        for (PersonalCardInfo personalCardInfo : personalCardInfos) {
            Integer userId = personalCardInfo.getRoomMember().getUserId();
            users.add(getUserByUserId(userId));
        }
        return users;
    }

    /**
     * 本轮玩家已经出牌，获取下一个出牌的玩家
     *
     * @param user 本轮已经出牌的玩家
     */
    private User getNextTouchMahjongUser(MahjongGameData mahjongGameData, User user) {
        // 本轮已经出牌的座位号
        Integer userSeat = null;
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            if (personalCardInfo.getRoomMember().getUserId().equals(user.getId())) {
                userSeat = personalCardInfo.getRoomMember().getSeat();
                break;
            }
        }

        // 获取下一个座位号
        Integer next = userSeat + 1;
        // 如果座位号next大于玩家人数，则座位号改为1，从头开始
        if (next > mahjongGameData.getPersonalCardInfos().size()) {
            next = 1;
        }

        Integer nextUserId = null;
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            if (personalCardInfo.getRoomMember().getSeat().equals(next)) {
                nextUserId = personalCardInfo.getRoomMember().getUserId();
            }
        }

        User nextUser = getUserByUserId(nextUserId);
        return nextUser;
    }


}
