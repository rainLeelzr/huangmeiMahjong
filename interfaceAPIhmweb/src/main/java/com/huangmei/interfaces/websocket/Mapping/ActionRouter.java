package com.huangmei.interfaces.websocket.Mapping;

import com.huangmei.commonhm.manager.getACard.GetACardManager;
import com.huangmei.commonhm.manager.operate.CanDoOperate;
import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.AfterPutOutCardManager;
import com.huangmei.commonhm.manager.qiangGang.QiangGangManager;
import com.huangmei.commonhm.model.*;
import com.huangmei.commonhm.model.mahjong.*;
import com.huangmei.commonhm.model.mahjong.vo.*;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.VersionRedis;
import com.huangmei.commonhm.redis.base.Redis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.service.UserService;
import com.huangmei.commonhm.service.VoteService;
import com.huangmei.commonhm.service.impl.GameService;
import com.huangmei.commonhm.util.*;
import com.huangmei.interfaces.monitor.MonitorManager;
import com.huangmei.interfaces.monitor.clientTouchMahjong.task.ClientTouchMahjongTask;
import com.huangmei.interfaces.monitor.clientTouchMahjong.toucher.CommonToucher;
import com.huangmei.interfaces.monitor.clientTouchMahjong.toucher.GangToucher;
import com.huangmei.interfaces.monitor.schedule.DismissRoomVoteTask;
import com.huangmei.interfaces.monitor.trusteeship.TrusteeshipTask;
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
@SuppressWarnings("unchecked")
public class ActionRouter {

    Logger log = LoggerFactory.getLogger(ActionRouter.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private VoteService voteService;

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
    private AfterPutOutCardManager afterPutOutCardManager;

    @Autowired
    private QiangGangManager qiangGangManager;

    @Autowired
    private GameRedis gameRedis;

    @Autowired
    private VersionRedis versionRedis;

    /**
     * 下一个玩家摸牌逻辑
     */
    private void handleGangTouchAMahjong(MahjongGameData mahjongGameData, User gangUser) {
        // 4个玩家，按座位号升序
        List<User> users = getRoomUsers(mahjongGameData.getPersonalCardInfos());

        // 给杠的用户在leftCards的开头摸一张牌，并广播
        monitorManager.watch(new ClientTouchMahjongTask
                .Builder()
                .setToucher(new GangToucher())
                .setGetACardManager(getACardManager)
                .setMessageManager(messageManager)
                .setMahjongGameData(mahjongGameData)
                .setUser(gangUser)
                .setUsers(users)
                .setGameRedis(gameRedis)
                .setVersionRedis(versionRedis)
                .setActionRouter(this)
                .build());
    }

    /**
     * 下一个玩家摸牌逻辑
     */
    private void handleCommonNextUserTouchAMahjong(Room room, MahjongGameData mahjongGameData) {
        User currentUser = getLastPutOutCardUser(mahjongGameData);

        handleCommonNextUserTouchAMahjong(room, mahjongGameData, currentUser);
    }

    /**
     * 下一个玩家摸牌逻辑
     */
    private void handleCommonNextUserTouchAMahjong(Room room, MahjongGameData mahjongGameData, User currentUser) {
        // 是否平局（没有牌可以摸）
        boolean isDraw = isDraw(mahjongGameData);
        if (isDraw) {
            Object[] result = gameService.draw(room, mahjongGameData);
            List<Score> scores = (List<Score>) result[0];
            List<SingleUserGameScoreVo> singleUserGameScoreVos = result[1] == null ? null : (List<SingleUserGameScoreVo>) result[1];

            //单局结算广播
            broadcastSingleScore(mahjongGameData, scores, room, null, null, null);

            // 总结算广播
            broadcastUserTotalScore(singleUserGameScoreVos, room.getId());
            return;
        }

        // 4个玩家，按座位号升序
        List<User> users = getRoomUsers(mahjongGameData.getPersonalCardInfos());

        // 一个玩家出牌后，轮到下一个玩家摸牌
        User nextUser = getNextTouchMahjongUser(mahjongGameData, currentUser);

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
                .setActionRouter(this)
                .build());
    }

    /**
     * 判断是否平局
     */
    private boolean isDraw(MahjongGameData mahjongGameData) {
        if (mahjongGameData.getLeftCards().size() == 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 处理操作链中，通知下一个人的操作
     */
    private void handleNextCanDoOperate(MahjongGameData mahjongGameData, CanDoOperate nextCanDoOperate) {
        // 可以操作的人
        User beOperateUser = getUserByUserId(nextCanDoOperate.getRoomMember().getUserId());

        for (PersonalCardInfo cardInfo : mahjongGameData.getPersonalCardInfos()) {
            ClientOperate clientOperate = new ClientOperate();
            clientOperate.setuId(beOperateUser.getUId());
            clientOperate.setOperatePids(Operate.parseToPids(nextCanDoOperate.getOperates()));
            clientOperate.setHandCardIds(Mahjong.parseToIds(cardInfo.getHandCards()));
            clientOperate.setPengMahjongIs(Mahjong.parseCombosToMahjongIds(cardInfo.getPengs()));
            clientOperate.setGangs(GangVo.parseFromGangCombos(cardInfo.getGangs()));
            clientOperate.setPlayedMahjongId(nextCanDoOperate.getSpecialMahjong().getId());
            clientOperate.setPlayerUId(getUserByUserId(nextCanDoOperate.getSpecialUserId()).getUId());

            messageManager.sendMessageByUserId(cardInfo.getRoomMember().getUserId(), new JsonResultY.Builder()
                    .setPid(PidValue.CLIENT_OPERATE)
                    .setError(CommonError.SYS_SUSSES)
                    .setData(clientOperate)
                    .build());
        }

        nextCanDoOperate.getOperates().add(Operate.GUO);
        gameRedis.saveWaitingClientOperate(nextCanDoOperate);

        dealTrustesshipTask(mahjongGameData, nextCanDoOperate);
    }

    /**
     * 处理是否需要服务端自动帮玩家进行操作（执行托管）
     */
    public void dealTrustesshipTask(MahjongGameData mahjongGameData, CanDoOperate nextCanDoOperate) {
        // 查找正在托管的用户
        Integer nextOperateUserId = nextCanDoOperate.getRoomMember().getUserId();
        List<Integer> trusteeshipUserIds = gameRedis.getTrusteeshipUserIds(mahjongGameData.getRoomId());
        for (Integer trusteeshipUserId : trusteeshipUserIds) {
            if (nextOperateUserId.equals(trusteeshipUserId)) {
                monitorManager.schedule(
                        new TrusteeshipTask.Builder()
                                .setActionRouter(this)
                                .setCanDoOperate(nextCanDoOperate)
                                .setMahjongGameData(mahjongGameData)
                                .setRoom(getRoomByUserIdOrRoomId(nextOperateUserId, mahjongGameData.getRoomId()))
                                .setUser(getUserByUserId(nextOperateUserId))
                                .build(),
                        500
                );
                break;
            }
        }
    }

    /**
     * 获取最后一个打出牌的人
     */
    private User getLastPutOutCardUser(MahjongGameData mahjongGameData) {
        return getUserByUserId(
                mahjongGameData
                        .getOutCards()
                        .get(mahjongGameData.getOutCards().size() - 1)
                        .getRoomMember()
                        .getUserId()
        );
    }

    /**
     * 获取房间用户
     */
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
     * @param currentUser 本轮已经出牌的玩家
     */
    private User getNextTouchMahjongUser(MahjongGameData mahjongGameData, User currentUser) {
        // 本轮已经出牌的座位号
        Integer userSeat = null;
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            if (personalCardInfo.getRoomMember().getUserId().equals(currentUser.getId())) {
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

    /**
     * 根据用户id或房间id获取房间对象
     */
    private Room getRoomByUserIdOrRoomId(Integer userId, Integer roomId) {
        WebSocketSession session = sessionManager.getByUserId(userId);

        Room room = null;
        if (session == null) {
            room = roomService.selectOne(roomId);
        } else {
            room = sessionManager.getRoom(session.getId());
            if (room == null) {
                room = roomService.selectOne(roomId);
            }
        }

        return room;
    }

    @Pid(PidValue.HEARTBEAT)
    public JsonResultY heartbeat(WebSocketSession session, JSONObject data) {
        return new JsonResultY.Builder()
                .setPid(PidValue.HEARTBEAT.getPid())
                .setError(CommonError.SYS_SUSSES)
                .build();
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
            if (loginType == 2 || loginType == 3 || loginType == 4) {
                sessionManager.userJoinRoom((Room) result.get(("room")), session);
                result.remove("room");

                if (loginType == 4) {
                    ReconnectionVo reconnectionVo = (ReconnectionVo) result.get("gameData");

                    Integer bankerUserId = reconnectionVo.getGameStart().getBankerUId();
                    User user = getUserByUserId(bankerUserId);
                    reconnectionVo.getGameStart().setBankerUId(user.getUId());

                    List<RoomMember> roomMembers = reconnectionVo.getRoomMembers();
                    for (RoomMember roomMember : roomMembers) {
                        User me = getUserByUserId(roomMember.getUserId());
                        roomMember.setUser(me);

                        roomMember.getPersonalCardVo().setuId(me.getUId());
                    }
                }

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
        if (roomMembers != null) {

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
        }
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
            result.put("uId", user.getUId());
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
        Map<String, Object> result = roomService.ready(user);
        Integer type = (Integer) result.get("type");

        boolean isFirstPutOutCard = false;
        List<Object[]> firstPutOutCardBroadcasts = new ArrayList<>(4);

        GameStartVo gameStartVo = null;

        MahjongGameData mahjongGameData = null;

        User bankerUser = null;

        List<User> users = null;

        if (type == 2) {
            isFirstPutOutCard = true;

            List<FirstPutOutCard> firstPutOutCards = (List<FirstPutOutCard>) result.get(GameService.PUT_OUT_HANDCARD_KEY);

            mahjongGameData = (MahjongGameData) result.get(MahjongGameData.class.getSimpleName());
            result.remove(MahjongGameData.class.getSimpleName());

            gameStartVo = (GameStartVo) result.get(GameStartVo.class.getSimpleName());
            result.remove(GameStartVo.class.getSimpleName());

            // 获取庄家uId
            Integer bankerUserId = gameStartVo.getBankerUId();
            bankerUser = getUserByUserId(bankerUserId);
            gameStartVo.setBankerUId(bankerUser.getUId());

            // 4个玩家，按座位号升序
            users = new ArrayList<>(firstPutOutCards.size());

            // 广播给4个用户第一次发牌
            for (FirstPutOutCard firstPutOutCard : firstPutOutCards) {
                Map<String, Object> myResult = new HashMap<>();
                myResult.put("type", 2);

                // 需要接受广播消息的用户uid
                Integer acceptBroadcastUserId = firstPutOutCard.getuId();
                User acceptBroadcastUser = getUserByUserId(acceptBroadcastUserId);
                firstPutOutCard.setuId(acceptBroadcastUser.getUId());

                users.add(acceptBroadcastUser);

                myResult.put(GameService.PUT_OUT_HANDCARD_KEY, firstPutOutCard);

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
        }
        result.remove(GameService.PUT_OUT_HANDCARD_KEY);
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.READY.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();

        // 广播玩家准备
        messageManager.sendMessageToRoomUsers(
                (result.get("roomId")).toString(),
                jsonResultY);

        if (isFirstPutOutCard) {

            for (Object[] firstPutOutCardBroadcast : firstPutOutCardBroadcasts) {
                // 广播游戏开始
                messageManager.sendMessageByUserId(
                        (Integer) firstPutOutCardBroadcast[0],
                        new JsonResultY.Builder()
                                .setPid(PidValue.GAME_START.getPid())
                                .setError(CommonError.SYS_SUSSES)
                                .setData(gameStartVo)
                                .build()
                );

                // 广播給客户端他们各自的牌
                messageManager.sendMessageByUserId(
                        (Integer) firstPutOutCardBroadcast[0],
                        (JsonResultY) firstPutOutCardBroadcast[1],
                        800
                );
            }

            // 庄家摸一张牌
            monitorManager.schedule(
                    new ClientTouchMahjongTask
                            .Builder()
                            .setToucher(new CommonToucher())
                            .setGetACardManager(getACardManager)
                            .setMessageManager(messageManager)
                            .setMahjongGameData(mahjongGameData)
                            .setUser(bankerUser)
                            .setUsers(users)
                            .setGameRedis(gameRedis)
                            .setVersionRedis(versionRedis)
                            .setActionRouter(this)
                            .build(),
                    1000);
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

        if (!(Boolean) result.get("result")) {//需要发起投票,开始计时任务
            //2分钟计时无响应默认同意
            DismissRoomVoteTask dismissRoomVoteTask = new DismissRoomVoteTask();
            dismissRoomVoteTask.setRoomService(roomService);
            dismissRoomVoteTask.setRoomId(roomId);

            monitorManager.schedule(dismissRoomVoteTask, 10 * 1000);
        }

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
        if ((User) result.get("user") != null) {
            sessionManager.userUpdate((User) result.get("user"), session);
        }

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

        if ((User) result.get("user") != null) {
            sessionManager.userUpdate((User) result.get("user"), session);
        }

        return new JsonResultY.Builder()
                .setPid(PidValue.FREE_COINS.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.BIND_PHONE)
    @LoginResource
    public JsonResultY bindPhone(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());

        Map<String, Object> result = userService.bindPhone(data, user);

        if ((User) result.get("user") != null) {
            sessionManager.userUpdate((User) result.get("user"), session);
        }

        return new JsonResultY.Builder()
                .setPid(PidValue.BIND_PHONE.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.BIND_PROMOTE_CODE)
    @LoginResource
    public JsonResultY bindPromoteCode(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());

        Map<String, Object> result = userService.bindPromoteCode(data, user);

        if ((User) result.get("user") != null) {
            sessionManager.userUpdate((User) result.get("user"), session);
        }

        return new JsonResultY.Builder()
                .setPid(PidValue.BIND_PROMOTE_CODE.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.TEN_WINS)
    @LoginResource
    public JsonResultY tenWins(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());

        Map<String, Object> result = userService.tenWins(data, user);

        if ((User) result.get("user") != null) {
            sessionManager.userUpdate((User) result.get("user"), session);
        }

        return new JsonResultY.Builder()
                .setPid(PidValue.TEN_WINS.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.GET_STANDINGS)
    @LoginResource
    public JsonResultY getStanding(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());
        Map<String, Object> result = userService.getStanding(room, user);

        return new JsonResultY.Builder()
                .setPid(PidValue.GET_STANDINGS.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.NUMBER_OF_PLAYERS)
    @LoginResource
    public JsonResultY numberOfPlayers(WebSocketSession session, JSONObject data)
            throws Exception {

        Map<String, Object> result = roomService.numberOfPlayers(data);

        return new JsonResultY.Builder()
                .setPid(PidValue.NUMBER_OF_PLAYERS.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.ROOM_INFO)
    @LoginResource
    public JsonResultY roomInfo(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = roomService.roomInfo(user);

        return new JsonResultY.Builder()
                .setPid(PidValue.ROOM_INFO.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.BUY)
    @LoginResource
    public JsonResultY buy(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = userService.buy(data, user);
        sessionManager.userUpdate((User) result.get("user"), session);

        return new JsonResultY.Builder()
                .setPid(PidValue.BUY.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
    }

    @Pid(PidValue.COMMUNICATION)
    @LoginResource
    public JsonResultY communication(WebSocketSession session, JSONObject data)
            throws Exception {
        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = roomService.communication(data, user);
        result.put("uId", user.getUId());
        Integer roomId = (Integer) result.get("roomId");
        result.remove("roomId");
        JsonResultY jsonResultY = new JsonResultY.Builder()
                .setPid(PidValue.COMMUNICATION.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();
        messageManager.sendMessageToRoomUsers(roomId.toString(), jsonResultY);
        return null;
    }

    @Pid(PidValue.HORN_SPEAK)
    @LoginResource
    public JsonResultY hornSpeak(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = userService.hornSpeak(data, user);
        if ((User) result.get("user") != null) {
            sessionManager.userUpdate((User) result.get("user"), session);
            JsonResultY jsonResultY = new JsonResultY.Builder()
                    .setPid(PidValue.HORN_SPEAK.getPid())
                    .setError(CommonError.SYS_SUSSES)
                    .setData(result)
                    .build();

            messageManager.sendMessageByUserId(user.getId(), jsonResultY);

            Map<Object, Object> temp = new HashMap<>();
            temp.putAll(result);
            temp.remove("user");

            JsonResultY jr = new JsonResultY.Builder()
                    .setPid(PidValue.HORN_SPEAK.getPid())
                    .setError(CommonError.SYS_SUSSES)
                    .setData(temp)
                    .build();

            messageManager.sendToAllUsers(jr);
            return null;
        } else {
            return new JsonResultY.Builder()
                    .setPid(PidValue.HORN_SPEAK.getPid())
                    .setError(CommonError.SYS_SUSSES)
                    .setData(result)
                    .build();
        }

    }

    @Pid(PidValue.SYSTEM_NOTICE)
    @LoginResource
    public JsonResultY systemNotice(WebSocketSession session, JSONObject data)
            throws Exception {

        User user = sessionManager.getUser(session.getId());
        Map<String, Object> result = userService.systemNotice(data, user);
        if ((User) result.get("user") != null) {
            sessionManager.userUpdate((User) result.get("user"), session);
        }

        JsonResultY jr = new JsonResultY.Builder()
                .setPid(PidValue.SYSTEM_NOTICE.getPid())
                .setError(CommonError.SYS_SUSSES)
                .setData(result)
                .build();

        messageManager.sendToAllUsers(jr);
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
        //Map<String, Object> mahjongGameDatas = gameService.putOutHandCard
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
    public JsonResultY playACard(WebSocketSession session, JSONObject data)
            throws Exception {

        int mahjongId = JsonUtil.getInt(data, "mahjongId");
        //long version = JsonUtil.getLong(data, "version");

        Mahjong playedMahjong = Mahjong.parse(mahjongId);

        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        handlePlayACard(room, user, playedMahjong);
        return null;
    }

    /**
     * 处理玩家打出一张牌逻辑
     */
    @SuppressWarnings("unchecked")
    public void handlePlayACard(Room room, User user, Mahjong playedMahjong) throws InstantiationException, IllegalAccessException {
        Map<String, Object> result = gameService.playAMahjong(room, user, playedMahjong);

        // 响应用户已经打出牌
        messageManager.sendMessageByUserId(
                user.getId(),
                new JsonResultY.Builder()
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
        gameRedis.deleteCanOperates(room.getId());

        MahjongGameData mahjongGameData = (MahjongGameData) result.get(MahjongGameData.class.getSimpleName());


        // 扫描其他用户是否有吃胡、大明杠、碰的操作
        List<CanDoOperate> canOperates =
                afterPutOutCardManager.scan(mahjongGameData, playedMahjong, user);

        if (canOperates.size() == 0) {
            handleCommonNextUserTouchAMahjong(room, mahjongGameData, user);
        } else {
            CanDoOperate firstCanDoOperate = canOperates.remove(0);

            handleNextCanDoOperate(mahjongGameData, firstCanDoOperate);

            // 如果还有玩家可以操作，则添加到排队列表
            if (canOperates.size() != 0) {
                gameRedis.saveCanOperates(canOperates);
            }

            //CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(firstCanDoOperate.getRoomMember().getRoomId());
        }
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
        PersonalCardInfo gangUserCarInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            GangBroadcast anGangBroadcast =
                    new GangBroadcast(
                            getUserByUserId(
                                    personalCardInfo.getRoomMember().getUserId()
                            ).getUId(),
                            user.getUId(),
                            mahjongGameData
                                    .getTouchMahjongs()
                                    .get(mahjongGameData.getTouchMahjongs().size() - 1)
                                    .getMahjong()
                                    .getId(),
                            toBeGangMahjongIds,
                            user.getUId(),
                            PidValue.YING_AN_GANG.getPid(),
                            Mahjong.parseToIds(gangUserCarInfo.getHandCards()),
                            Mahjong.parseCombosToMahjongIds(gangUserCarInfo.getPengs()),
                            GangVo.parseFromGangCombos(gangUserCarInfo.getGangs())
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
                .setActionRouter(this)
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
        PersonalCardInfo gangUserCarInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            GangBroadcast yingJiaGangBroadcast =
                    new GangBroadcast(
                            getUserByUserId(
                                    personalCardInfo.getRoomMember().getUserId()
                            ).getUId(),
                            user.getUId(),
                            toBeJiaGangMahjongId,
                            Mahjong.parseToIds(jiaGangCombo.mahjongs),
                            user.getUId(),
                            PidValue.YING_AN_GANG.getPid(),
                            Mahjong.parseToIds(gangUserCarInfo.getHandCards()),
                            Mahjong.parseCombosToMahjongIds(gangUserCarInfo.getPengs()),
                            GangVo.parseFromGangCombos(gangUserCarInfo.getGangs())
                    );
            messageManager.sendMessageByUserId(
                    personalCardInfo.getRoomMember().getUserId(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.GANG_BROADCAST)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(yingJiaGangBroadcast)
                            .build());
        }

        // 删除clientOperateQueue
        gameRedis.deleteCanOperates(room.getId());

        // 判断其他玩家有没有抢杠
        scanAnyUserQiangGangHandler(mahjongGameData, mahjong, user);

        return null;
    }

    /**
     * 扫描有没有用户可以抢杠
     * 没有，则给user发一张牌
     * 有则广播可操作列表
     *
     * @param mahjongGameData 游戏数据
     * @param mahjong         别人打出或自己摸到的麻将
     * @param user            需要大明杠或者加杠的玩家
     */
    private void scanAnyUserQiangGangHandler(MahjongGameData mahjongGameData, Mahjong mahjong, User user) throws InstantiationException, IllegalAccessException {
        // 判断其他玩家有没有抢杠
        List<CanDoOperate> canOperates =
                qiangGangManager.scan(mahjongGameData, mahjong, user);

        if (canOperates.size() == 0) {
            // 给杠的用户在leftCards的开头摸一张牌，并广播
            handleGangTouchAMahjong(mahjongGameData, user);
        } else {
            CanDoOperate firstCanDoOperate = canOperates.remove(0);

            handleNextCanDoOperate(mahjongGameData, firstCanDoOperate);

            // 如果还有玩家可以操作，则添加到排队列表
            if (canOperates.size() != 0) {
                gameRedis.saveCanOperates(canOperates);
            }
        }
    }

    @Pid(PidValue.YING_DA_MING_GANG)
    @LoginResource
    public JsonResultY daMingGang(WebSocketSession session, JSONObject data) throws InstantiationException, IllegalAccessException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        // 0,1,2下标是原有的麻将，3是别人打的麻将
        List<Integer> toBeYingDaMingGangMahjongId = JsonUtil.getIntegerList(data, "mahjongIds");
        if (toBeYingDaMingGangMahjongId.size() != 4) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        List<Mahjong> mahjongs = Mahjong.parseFromIds(toBeYingDaMingGangMahjongId);

        // 判断4只麻将是否一样
        if (!Mahjong.isSame(mahjongs)) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        // 别人打出的麻将
        Mahjong playedMahjong = mahjongs.get(mahjongs.size() - 1);

        Object[] result = gameService.yingDaMingGang(user, room, mahjongs);
        MahjongGameData mahjongGameData = (MahjongGameData) result[0];
        Combo yingDaMingGangCombo = (Combo) result[1];

        // 响应玩家通过大明杠执行逻辑
        messageManager.send(
                session,
                new JsonResultY.Builder()
                        .setPid(PidValue.YING_DA_MING_GANG)
                        .setError(CommonError.SYS_SUSSES)
                        .build());

        // 广播玩家执行大明杠
        PersonalCardInfo gangUserCarInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            GangBroadcast daMingGangBroadcast =
                    new GangBroadcast(
                            getUserByUserId(
                                    personalCardInfo.getRoomMember().getUserId()
                            ).getUId(),
                            getUserByUserId(mahjongGameData
                                    .getOutCards()
                                    .get(mahjongGameData.getOutCards().size() - 1)
                                    .getRoomMember()
                                    .getUserId()
                            ).getUId(),
                            toBeYingDaMingGangMahjongId.get(3),
                            Mahjong.parseToIds(yingDaMingGangCombo.mahjongs),
                            user.getUId(),
                            PidValue.YING_DA_MING_GANG.getPid(),
                            Mahjong.parseToIds(gangUserCarInfo.getHandCards()),
                            Mahjong.parseCombosToMahjongIds(gangUserCarInfo.getPengs()),
                            GangVo.parseFromGangCombos(gangUserCarInfo.getGangs())
                    );
            messageManager.sendMessageByUserId(
                    personalCardInfo.getRoomMember().getUserId(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.GANG_BROADCAST)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(daMingGangBroadcast)
                            .build());
        }

        // 删除clientOperateQueue
        gameRedis.deleteCanOperates(room.getId());

        // 判断其他玩家有没有抢杠
        scanAnyUserQiangGangHandler(mahjongGameData, playedMahjong, user);

        return null;
    }

    @Pid(PidValue.YING_PENG)
    @LoginResource
    public JsonResultY peng(WebSocketSession session, JSONObject data) {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        // 0,1下标是原有的麻将，2是别人打的麻将
        List<Integer> toBePengMahjongIds = JsonUtil.getIntegerList(data, "mahjongIds");
        if (toBePengMahjongIds.size() != 3) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        List<Mahjong> mahjongs = Mahjong.parseFromIds(toBePengMahjongIds);

        // 判断3只麻将是否一样
        if (!Mahjong.isSame(mahjongs)) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        Object[] result = gameService.peng(user, room, mahjongs);
        MahjongGameData mahjongGameData = (MahjongGameData) result[0];
        Combo yingDaMingGangCombo = (Combo) result[1];

        // 响应玩家通过碰执行逻辑
        messageManager.send(
                session,
                new JsonResultY.Builder()
                        .setPid(PidValue.YING_PENG)
                        .setError(CommonError.SYS_SUSSES)
                        .build());


        // 广播玩家执行碰
        PersonalCardInfo pengUserCarInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            messageManager.sendMessageByUserId(
                    personalCardInfo.getRoomMember().getUserId(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.PENG_BROADCAST)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(new PengBroadcast(
                                    getUserByUserId(personalCardInfo.getRoomMember().getUserId()).getUId(),
                                    getUserByUserId(mahjongGameData
                                            .getOutCards()
                                            .get(mahjongGameData.getOutCards().size() - 1)
                                            .getRoomMember()
                                            .getUserId()
                                    ).getUId(),
                                    mahjongGameData
                                            .getOutCards()
                                            .get(mahjongGameData.getOutCards().size() - 1)
                                            .getMahjong()
                                            .getId(),
                                    Mahjong.parseToIds(yingDaMingGangCombo.mahjongs),
                                    user.getUId(),
                                    PidValue.YING_PENG.getPid(),
                                    Mahjong.parseToIds(pengUserCarInfo.getHandCards()),
                                    Mahjong.parseCombosToMahjongIds(pengUserCarInfo.getPengs()),
                                    GangVo.parseFromGangCombos(pengUserCarInfo.getGangs())
                            ))
                            .build()
            );
        }

        // 删除clientOperateQueue
        gameRedis.deleteCanOperates(room.getId());

        return null;
    }

    @Pid(PidValue.QIANG_GANG_HU)
    @LoginResource
    @SuppressWarnings("unchecked")
    public JsonResultY qiangGangHu(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        // 别人打出来，我抢杠的麻将,或者加杠的麻将
        Mahjong qiangGangMahjong = Mahjong.parse(JsonUtil.getInt(data, "mahjongId"));
        Integer gangUserUId = JsonUtil.getInt(data, "gangUserUId");
        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(gangUserUId));
        User gangUser = userService.selectOne(userCriteria);

        Object[] result = gameService.qiangGangHu(room, user, gangUser, qiangGangMahjong);
        List<Score> scores = (List<Score>) result[0];
        MahjongGameData mahjongGameData = (MahjongGameData) result[1];
        Mahjong specialMahjong = (Mahjong) result[2];
        List<SingleUserGameScoreVo> singleUserGameScoreVos = result[3] == null ? null : (List<SingleUserGameScoreVo>) result[3];
        List<Integer> cancelTrusteeshipUserIds = (List<Integer>) result[4];

        //单局结算广播
        broadcastSingleScore(mahjongGameData, scores, room, user.getId(), gangUser.getId(), specialMahjong);

        // 总结算广播
        broadcastUserTotalScore(singleUserGameScoreVos, room.getId());

        // 广播用户“取消托管”
        if (!cancelTrusteeshipUserIds.isEmpty()) {
            for (Integer cancelTrusteeshipUserId : cancelTrusteeshipUserIds) {
                messageManager.sendMessageToRoomUsers(
                        room.getId().toString(),
                        new JsonResultY.Builder()
                                .setPid(PidValue.REMOVE_TRUSTEESHIP)
                                .setError(CommonError.SYS_SUSSES)
                                .setData(getUserByUserId(cancelTrusteeshipUserId).getUId())
                                .build()
                );
            }
        }

        return null;
    }

    @Pid(PidValue.GUO)
    @LoginResource
    public JsonResultY guo(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());
        handleGuo(room, user);

        return null;
    }

    /**
     * 处理“过”
     *
     * @param room 选择“过”的用户所在的房间
     * @param user 选择“过”的用户
     */
    public void handleGuo(Room room, User user) {
        // 下一个可以操作的人
        Object[] result = gameService.guo(user, room);
        CanDoOperate nextCanDoOperate = (CanDoOperate) result[0];
        CanDoOperate waitingClientOperate = (CanDoOperate) result[1];
        MahjongGameData mahjongGameData = (MahjongGameData) result[2];

        // 通知玩家通过“过”验证
        messageManager.sendMessageByUserId(
                user.getId(),
                new JsonResultY.Builder()
                        .setPid(PidValue.GUO)
                        .setError(CommonError.SYS_SUSSES)
                        .build());

        if (nextCanDoOperate != null) {
            handleNextCanDoOperate(mahjongGameData, nextCanDoOperate);
        } else {
            // 判断发起操作链的是玩家打出一张牌，还是明杠（抢杠）
            // 如果是明杠发起的其他用户操作链，则明杠用户继续执行杠逻辑，即摸牌
            // 如果是玩家打出一张牌发起的其他用户操作链，则打出一张牌玩家的下一个玩家摸牌
            if (waitingClientOperate.getOperates().contains(Operate.QIANG_DA_MING_GANG_HU)
                    || waitingClientOperate.getOperates().contains(Operate.QIANG_JIA_GANG_HU)) {
                // 别人大明杠或加杠，自己抢大明杠或加杠的情况
                // 找到杠玩家的uid
                Integer gangUserId = mahjongGameData
                        .getTouchMahjongs()
                        .get(mahjongGameData.getTouchMahjongs().size() - 1)
                        .getUserId();
                User gangUser = getUserByUserId(gangUserId);
                handleGangTouchAMahjong(mahjongGameData, gangUser);
            } else if (waitingClientOperate.getOperates().contains(Operate.CHI_YING_PENG_PENG_HU)
                    || waitingClientOperate.getOperates().contains(Operate.CHI_YING_PING_HU)
                    || waitingClientOperate.getOperates().contains(Operate.CHI_YING_QI_DUI_HU)
                    || waitingClientOperate.getOperates().contains(Operate.CHI_RUAN_PENG_PENG_HU)
                    || waitingClientOperate.getOperates().contains(Operate.CHI_RUAN_PING_HU)
                    || waitingClientOperate.getOperates().contains(Operate.CHI_RUAN_QI_DUI_HU)
                    || waitingClientOperate.getOperates().contains(Operate.QIANG_GANG_HU)
                    || waitingClientOperate.getOperates().contains(Operate.YING_DA_MING_GANG)
                    || waitingClientOperate.getOperates().contains(Operate.YING_PENG)) {
                // 别人打牌，自己可以吃胡、大明杠、碰的情况
                handleCommonNextUserTouchAMahjong(room, mahjongGameData);
            } else {
                // 自己摸牌，自摸胡、暗杠、加杠的情况，自己需要主动打一张牌
            }
        }
    }

    /**
     * 单局结算广播
     */
    private void broadcastSingleScore(
            MahjongGameData mahjongGameData,
            List<Score> scores,
            Room room,
            Integer winnerUserId,
            Integer specialUserId,
            Mahjong specialMahjong) {
        SingleScoreVo singleScoreVo = new SingleScoreVo();
        List<SingleUserScoreVo> singleUserScoreVos = new ArrayList<>(scores.size());
        singleScoreVo.setSingleUserScoreVos(singleUserScoreVos);
        for (Score score : scores) {
            SingleUserScoreVo singleUserScoreVo = new SingleUserScoreVo();
            singleUserScoreVos.add(singleUserScoreVo);

            User tempUser = getUserByUserId(score.getUserId());
            PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), score.getUserId());

            singleUserScoreVo.setNickName(tempUser.getNickName());
            singleUserScoreVo.setImage(tempUser.getImage());

            singleUserScoreVo.setPaoShu(score.getPaoNum());
            singleUserScoreVo.setScore(score.getScore());
            singleUserScoreVo.setState(SingleUserScoreVo.parseToState(score.getWinType()));

            // 设置碰
            singleUserScoreVo.setPeng(new ArrayList<Integer>(personalCardInfo.getPengs().size()));
            for (Combo combo : personalCardInfo.getPengs()) {
                singleUserScoreVo.getPeng().add(combo.getMahjongs().get(0).getNumber());
            }

            // 设置杠
            singleUserScoreVo.setGang(new ArrayList<Integer>(personalCardInfo.getGangs().size()));
            for (Combo combo : personalCardInfo.getGangs()) {
                singleUserScoreVo.getGang().add(combo.getMahjongs().get(0).getNumber());
            }

            //设置手卡
            singleUserScoreVo.setCard(new ArrayList<Integer>(personalCardInfo.getHandCards().size()));
            for (Mahjong mahjong : personalCardInfo.getHandCards()) {
                singleUserScoreVo.getCard().add(mahjong.getNumber());
            }

            if (score.getUserId().equals(winnerUserId)) {
                singleUserScoreVo.setOtherCard(specialMahjong.getNumber());
            } else {
                if (score.getUserId().equals(specialUserId)) {
                    singleUserScoreVo.setOtherCard(specialMahjong.getNumber());
                } else {
                    singleUserScoreVo.setOtherCard(0);
                }
            }

        }
        messageManager.sendMessageToRoomUsers(
                room.getId().toString(),
                new JsonResultY.Builder()
                        .setPid(PidValue.SINGLE_SCORE)
                        .setError(CommonError.SYS_SUSSES)
                        .setData(singleScoreVo)
                        .build());
    }

    @Pid(PidValue.YING_ZI_MO)
    @LoginResource
    @SuppressWarnings("unchecked")
    public JsonResultY yingZiMo(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        Object[] result = gameService.yingZiMo(room, user);
        List<Score> scores = (List<Score>) result[0];
        MahjongGameData mahjongGameData = (MahjongGameData) result[1];
        Mahjong specialMahjong = (Mahjong) result[2];
        List<SingleUserGameScoreVo> singleUserGameScoreVos = result[3] == null ? null : (List<SingleUserGameScoreVo>) result[3];
        List<Integer> cancelTrusteeshipUserIds = (List<Integer>) result[4];

        //单局结算广播
        broadcastSingleScore(mahjongGameData, scores, room, user.getId(), user.getId(), specialMahjong);

        // 总结算广播
        broadcastUserTotalScore(singleUserGameScoreVos, room.getId());

        // 广播用户“取消托管”
        if (!cancelTrusteeshipUserIds.isEmpty()) {
            for (Integer cancelTrusteeshipUserId : cancelTrusteeshipUserIds) {
                messageManager.sendMessageToRoomUsers(
                        room.getId().toString(),
                        new JsonResultY.Builder()
                                .setPid(PidValue.REMOVE_TRUSTEESHIP)
                                .setError(CommonError.SYS_SUSSES)
                                .setData(getUserByUserId(cancelTrusteeshipUserId).getUId())
                                .build()
                );
            }
        }

        return null;
    }

    /**
     * 总结算广播
     */
    private void broadcastUserTotalScore(List<SingleUserGameScoreVo> singleUserGameScoreVos, Integer roomId) {
        if (singleUserGameScoreVos != null) {
            for (SingleUserGameScoreVo singleUserGameScoreVo : singleUserGameScoreVos) {
                Integer userId = singleUserGameScoreVo.getuId();
                User user = getUserByUserId(userId);
                singleUserGameScoreVo.setuId(user.getUId());
                singleUserGameScoreVo.setImage(user.getImage());
                singleUserGameScoreVo.setNickName(user.getNickName());
            }
            messageManager.sendMessageToRoomUsers(
                    roomId.toString(),
                    new JsonResultY.Builder()
                            .setPid(PidValue.TOTAL_SCORE)
                            .setError(CommonError.SYS_SUSSES)
                            .setData(singleUserGameScoreVos)
                            .build()
            );
        }
    }

    @Pid(PidValue.RUAN_ZI_MO)
    @LoginResource
    @SuppressWarnings("unchecked")
    public JsonResultY ruanZiMo(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        Object[] result = gameService.ruanZiMo(room, user);
        List<Score> scores = (List<Score>) result[0];
        MahjongGameData mahjongGameData = (MahjongGameData) result[1];
        Mahjong specialMahjong = (Mahjong) result[2];
        List<SingleUserGameScoreVo> singleUserGameScoreVos = result[3] == null ? null : (List<SingleUserGameScoreVo>) result[3];
        List<Integer> cancelTrusteeshipUserIds = (List<Integer>) result[4];

        //单局结算广播
        broadcastSingleScore(mahjongGameData, scores, room, user.getId(), user.getId(), specialMahjong);

        // 总结算广播
        broadcastUserTotalScore(singleUserGameScoreVos, room.getId());

        // 广播用户“取消托管”
        if (!cancelTrusteeshipUserIds.isEmpty()) {
            for (Integer cancelTrusteeshipUserId : cancelTrusteeshipUserIds) {
                messageManager.sendMessageToRoomUsers(
                        room.getId().toString(),
                        new JsonResultY.Builder()
                                .setPid(PidValue.REMOVE_TRUSTEESHIP)
                                .setError(CommonError.SYS_SUSSES)
                                .setData(getUserByUserId(cancelTrusteeshipUserId).getUId())
                                .build()
                );
            }
        }
        return null;
    }

    @Pid(PidValue.RUAN_CHI_HU)
    @LoginResource
    @SuppressWarnings("unchecked")
    public JsonResultY ruanChiHu(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        Object[] result = gameService.ruanChiHu(room, user);
        List<Score> scores = (List<Score>) result[0];
        MahjongGameData mahjongGameData = (MahjongGameData) result[1];
        Mahjong specialMahjong = (Mahjong) result[2];
        List<SingleUserGameScoreVo> singleUserGameScoreVos = result[3] == null ? null : (List<SingleUserGameScoreVo>) result[3];
        List<Integer> cancelTrusteeshipUserIds = (List<Integer>) result[4];

        OutCard outCard = mahjongGameData.getOutCards().get(mahjongGameData.getOutCards().size() - 1);

        //单局结算广播
        broadcastSingleScore(
                mahjongGameData,
                scores,
                room,
                user.getId(),
                outCard.getRoomMember().getUserId(),
                specialMahjong);

        // 总结算广播
        broadcastUserTotalScore(singleUserGameScoreVos, room.getId());

        // 广播用户“取消托管”
        if (!cancelTrusteeshipUserIds.isEmpty()) {
            for (Integer cancelTrusteeshipUserId : cancelTrusteeshipUserIds) {
                messageManager.sendMessageToRoomUsers(
                        room.getId().toString(),
                        new JsonResultY.Builder()
                                .setPid(PidValue.REMOVE_TRUSTEESHIP)
                                .setError(CommonError.SYS_SUSSES)
                                .setData(getUserByUserId(cancelTrusteeshipUserId).getUId())
                                .build()
                );
            }
        }

        return null;
    }

    @Pid(PidValue.YING_CHI_HU)
    @LoginResource
    @SuppressWarnings("unchecked")
    public JsonResultY yingChiHu(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        Object[] result = gameService.yingChiHu(room, user);
        List<Score> scores = (List<Score>) result[0];
        MahjongGameData mahjongGameData = (MahjongGameData) result[1];
        Mahjong specialMahjong = (Mahjong) result[2];
        List<SingleUserGameScoreVo> singleUserGameScoreVos = result[3] == null ? null : (List<SingleUserGameScoreVo>) result[3];
        List<Integer> cancelTrusteeshipUserIds = (List<Integer>) result[4];

        OutCard outCard = mahjongGameData.getOutCards().get(mahjongGameData.getOutCards().size() - 1);

        //单局结算广播
        broadcastSingleScore(
                mahjongGameData,
                scores,
                room,
                user.getId(),
                outCard.getRoomMember().getUserId(),
                specialMahjong);

        // 总结算广播
        broadcastUserTotalScore(singleUserGameScoreVos, room.getId());

        // 广播用户“取消托管”
        if (!cancelTrusteeshipUserIds.isEmpty()) {
            for (Integer cancelTrusteeshipUserId : cancelTrusteeshipUserIds) {
                messageManager.sendMessageToRoomUsers(
                        room.getId().toString(),
                        new JsonResultY.Builder()
                                .setPid(PidValue.REMOVE_TRUSTEESHIP)
                                .setError(CommonError.SYS_SUSSES)
                                .setData(getUserByUserId(cancelTrusteeshipUserId).getUId())
                                .build()
                );
            }
        }
        return null;
    }

    /**
     * 玩家主动调用托管
     */
    @Pid(PidValue.ADD_TRUSTEESHIP)
    @LoginResource
    public JsonResultY addTrusteeshipUser(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        if (room == null) {
            throw CommonError.ROOM_USER_NOT_IN_ROOM.newException();
        }

        Object[] result = gameService.addTrusteeshipUser(room, user);


        // 广播用户“托管”
        messageManager.sendMessageToRoomUsers(
                room.getId().toString(),
                new JsonResultY.Builder()
                        .setPid(PidValue.ADD_TRUSTEESHIP)
                        .setError(CommonError.SYS_SUSSES)
                        .setData(user.getUId())
                        .build()
        );

        // 判断当前游戏是否在等待托管用户操作
        dealTrustesshipTask((MahjongGameData) result[1], (CanDoOperate) result[0]);

        return null;
    }

    /**
     * 玩家主动取消托管
     */
    @Pid(PidValue.REMOVE_TRUSTEESHIP)
    @LoginResource
    public JsonResultY removeTrusteeshipUser(WebSocketSession session, JSONObject data) throws IllegalAccessException, InstantiationException {
        User user = sessionManager.getUser(session.getId());
        Room room = sessionManager.getRoom(session.getId());

        if (room == null) {
            throw CommonError.ROOM_USER_NOT_IN_ROOM.newException();
        }

        Object[] result = gameService.removeTrusteeshipUser(room, user);

        // 广播用户“取消托管”
        messageManager.sendMessageToRoomUsers(
                room.getId().toString(),
                new JsonResultY.Builder()
                        .setPid(PidValue.REMOVE_TRUSTEESHIP)
                        .setError(CommonError.SYS_SUSSES)
                        .setData(user.getUId())
                        .build()
        );

        return null;
    }


    /**
     * 处理客户端断线
     * 用户进入了房间，未准备时，踢出房间
     * 用户进入了房间，已准备时，踢出房间
     * 用户进入了房间，已开始游戏，不用踢出房间，如果在金币场，则自动设置用户为托管状态，如果在好友场，则不用自动设置用户为托管状态。
     * 用户进入了房间，单局游戏，不用踢出房间
     */
    public void dealDisconnection(WebSocketSession session) {
        User user = sessionManager.getUser(session.getId());
        if (user == null) {
            return;
        }

        Object[] result = gameService.dealDisconnection(user);

        // 用户不在游戏中，无需处理
        if (result == null) {
            return;
        }

        CanDoOperate waitingClientOperate = (CanDoOperate) result[0];
        MahjongGameData mahjongGameData = (MahjongGameData) result[1];
        Room room = (Room) result[2];
        RoomMember roomMember = (RoomMember) result[3];

        // 广播用户“托管”
        messageManager.sendMessageToRoomUsers(
                room.getId().toString(),
                new JsonResultY.Builder()
                        .setPid(PidValue.ADD_TRUSTEESHIP)
                        .setError(CommonError.SYS_SUSSES)
                        .setData(user.getUId())
                        .build()
        );

        // 判断当前游戏是否在等待托管用户操作，是则自动打牌
        dealTrustesshipTask(mahjongGameData, waitingClientOperate);
    }
}
