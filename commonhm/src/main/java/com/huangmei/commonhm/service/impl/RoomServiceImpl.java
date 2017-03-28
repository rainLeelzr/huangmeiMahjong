package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RoomDao;
import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.dao.UserDao;
import com.huangmei.commonhm.model.Entity;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.RoomRedis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.CommonUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RoomServiceImpl extends BaseServiceImpl<Integer, Room> implements RoomService {

    @Autowired
    private GameService gameService;

    @Autowired
    private RoomDao dao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoomMemberDao roomMemberDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private RoomRedis roomRedis;

    /**
     * 创建房间逻辑
     * 根据有无房间号来判断是创建好友房还是创建金币房
     * 若玩家还在房间中就不能再创建房间
     * 创建好友房前要判断用户是否有足够的钻石支付开房费
     *
     * @param data
     * @return
     */
    public Map<String, Object> createRoom(JSONObject data) {

        Map<String, Object> result = new HashMap<String, Object>(3);
        String uId = (String) data.get("uId");
        String times = (String) data.get("times");
        Integer multiple = (Integer) data.get("multiple");
        Integer payType = (Integer) data.get("payType");
        Integer type = data.getInt("type");
        Integer diamond = (Integer) data.get("diamond");

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = userDao.selectOne(userCriteria);

        RoomMember roomMember = new RoomMember();
        roomMember.setUserId(user.getId());
        roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);
        if (roomMember == null) {//玩家没有在房间中
            if (user != null) {
                Room room = new Room();
                room.setCreatedUserId(user.getId());
                room.setType(type);
                room.setPlayers(Room.playerLimit);
                room.setCreatedTime(new Date());
                room.setStart(Room.start.UNSTART.getCode());
                room.setState(Room.state.wait.getCode());
                room.setRoomCode(Integer.parseInt(CommonUtil.createRandomNumeric(4)));
                if (multiple != null) {//创建金币房
                    result = createCoinRoom(multiple, room, result);

                } else {//创建好友房
                    if (user.getDiamond() >= (payType == Room.payType.PAY_BY_ONE.getCode() ? diamond : diamond / Room.playerLimit)) {
                        result = createFriendRoom(user, times, diamond, payType, room, result);
                    } else {
                        throw CommonError.USER_LACK_DIAMONDS.newException();
                    }
                }
                result.put("user",user);
                return result;
            } else {
                throw CommonError.USER_NOT_EXIST.newException();
            }
        } else {
            throw CommonError.ROOM_USER_IN_ROOM.newException();
        }
    }

    private Map<String, Object> createCoinRoom(Integer multiple, Room room, Map<String, Object> result) {
        room.setMultiple(multiple);
        dao.save(room);
        result.put("room", room);
        RoomMember roomMember = createRoomMember(room, room.getCreatedUserId());
        result.put("roomMember", roomMember);
        roomRedis.createRoom(room, roomMember);
        return result;
    }

    private Map<String, Object> createFriendRoom(User user, String times, Integer diamond, Integer payType, Room room, Map<String, Object> result) {
        room.setDiamond(diamond);
        room.setPayType(payType);
        room.setTimes(times);
        dao.save(room);
        result.put("room", room);
        RoomMember roomMember = createRoomMember(room, room.getCreatedUserId());
        result.put("roomMember", roomMember);
        roomRedis.createRoom(room, roomMember);

        userDao.update(user);
        return result;
    }

    private RoomMember createRoomMember(Room room, Integer createdUserId) {
        RoomMember roomMember = new RoomMember();
        roomMember.setState(RoomMember.state.UNREADY.getCode());
        roomMember.setJoinTime(new Date());
        roomMember.setRoomId(room.getId());
        roomMember.setSeat(1);
        roomMember.setUserId(createdUserId);
        roomMemberDao.save(roomMember);
        return roomMember;

    }

    private Map<String, Object> joinRoomMember(Room room, Long count, User user, Map<String, Object> result) {
        RoomMember roomMember = createRoomMember(room, user.getId());
        roomMember.setSeat((int) (count + 1));
        roomMemberDao.update(roomMember);
        result.put("room", room);
        //查出房间中所有玩家
        Set<RoomMember> roomMembers = roomRedis.getRoomMembers(roomMember.getRoomId().toString());
        result.put("roomMembers",roomMembers);
        roomRedis.joinRoom(roomMember);
        return result;

    }

    /**
     * 加入房间逻辑
     * 根据有无房号来就加入好友房和金币房
     * 玩家在房间中的时候不能再加入房间
     * 其中加入好友房是需要判断房间的支付方式是一人支付还是四人支付
     * 四人支付的话判断玩家是否有足够钻石支付房费
     * 而加入金币房判断是快速加入还是进入指定金币场
     * 都要判断玩家的金币是否达到加入房间的条件
     * 快速加入能根据玩家的金币数进入对相应的金币场
     * 所有金币房间都没有空位时,系统自动创建房间
     *
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> joinRoom(JSONObject data) {
        Map<String, Object> result = new HashMap<>(2);

        String uId = (String) data.get("uId");
        Integer roomCode = (Integer) data.get("roomCode");
        Integer multiple = (Integer) data.get("multiple");

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = userDao.selectOne(userCriteria);

        RoomMember roomMember = new RoomMember();
        roomMember.setUserId(user.getId());
        roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);

        if (user != null) {
            if (roomCode != null) {//进入好友场
                if (roomMember == null) {//玩家没有在房间中
                    result = joinFriendRoom(user, roomCode, result);
                } else {
                    throw CommonError.ROOM_USER_IN_ROOM.newException();
                }
            } else {//进入金币场

                if (roomMember != null) {//玩家已在房间中,即为换桌加入(加入到原来的等级的房间)
                    Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
                    roomCriteria.setId(Entity.Value.eq(roomMember.getRoomId()));
                    Room room = dao.selectOne(roomCriteria);
                    data.put("roomCode", room.getRoomCode());
                    outRoom(data);
                    multiple = room.getMultiple();
                }
                if (multiple != null) {//进入指定金币场
                    if (user.getCoin() >= multiple) {
                        result = joinCoinRoom(multiple, user, result);
                        if (result == null) {//所有房间都已经满人,系统自动创建金币房间
                            result = createRoom(data);
                        }
                    } else {
                        throw CommonError.USER_LACK_COINS.newException();
                    }
                } else {//快速加入

                    if (user.getCoin() >= Room.multiple.COINS_WITH_2000.getCode() && user.getCoin() < Room.multiple.COINS_WITH_20000.getCode()) {
                        multiple = Room.multiple.COINS_WITH_2000.getCode();
                    } else if (user.getCoin() >= Room.multiple.COINS_WITH_20000.getCode()) {
                        multiple = Room.multiple.COINS_WITH_20000.getCode();
                    } else {
                        throw CommonError.USER_LACK_COINS.newException();
                    }
                    result = joinCoinRoom(multiple, user, result);
                    if (result == null) {//所有房间都已经满人或没有对应的金币场房间,系统自动创建金币房间
                        result = createRoom(data);
                    }
                }
            }
            ArrayList<User> users = new ArrayList<>();
            Set<RoomMember>  roomMembers = (Set<RoomMember>) result.get("roomMembers");
            for (RoomMember member : roomMembers) {
                Entity.UserCriteria uc = new Entity.UserCriteria();
                uc.setId(Entity.Value.eq(member.getUserId()));
                user = userDao.selectOne(userCriteria);
                users.add(user);
            }
            result.put("users",users);
            return result;
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }


    private Map<String, Object> joinCoinRoom(Integer multiple, User user, Map<String, Object> result) {
        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setMultiple(Entity.Value.eq(multiple));
        List<Room> rooms = dao.selectList(roomCriteria);
        if (rooms.size() > 0) {
            for (Room room : rooms) {
                Long count = roomRedis.getRoomMemberCount(room.getId().toString());
                if (count < Room.playerLimit) {
                    return joinRoomMember(room, count, user, result);
                }
            }
        }
        return null;
    }

    private Map<String, Object> joinFriendRoom(User user, Integer roomCode, Map<String, Object> result) {
        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
        Room room = dao.selectOne(roomCriteria);

        if (room != null && room.getType() == Room.type.FRIENDS_ROOM.getCode()) {
            Long count = roomRedis.getRoomMemberCount(room.getId().toString());
            if (count < Room.playerLimit) {

                return joinRoomMember(room, count, user, result);
            } else {
                throw CommonError.ROOM_FULL.newException();
            }
        } else {
            throw CommonError.ROOM_NOT_EXIST.newException();
        }
    }

    /**
     * 退出房间
     *
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> outRoom(JSONObject data) {
        Map<String, Object> result = new HashMap<>(2);

        String uId = (String) data.get("uId");
        Integer roomCode = (Integer) data.get("roomCode");

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = userDao.selectOne(userCriteria);

        if (user != null) {
            RoomMember roomMember = new RoomMember();
            roomMember.setUserId(user.getId());
            roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);
            if (roomMember != null) {//需要在房间中才能退出房间
                Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
                roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
                Room room = dao.selectOne(roomCriteria);

                if (room != null) {
                    Long count = roomRedis.getRoomMemberCount(room.getId().toString());
                    if (count > 1) {//房间人数大于1人时,退出房间后房间状态为待开始状态
                        room.setState(Room.state.wait.getCode());
                    } else {//房间只剩下1个人时,退出房间后房间状态为解散状态
                        room.setState(Room.state.DISMISS.getCode());
                        room.setLastLoginTime(new Date());
                        roomRedis.dismissRoom(room);
                    }
                    dao.update(room);
                } else {
                    throw CommonError.ROOM_NOT_EXIST.newException();
                }
                roomRedis.editRoom(roomMember);//移除redis中的房间成员

                //座位顺序比退出玩家位置大的玩家都需要退后一位
                Set<RoomMember> roomMembers = roomRedis.getRoomMembers(room.getId().toString());
                for (RoomMember member : roomMembers) {
                    if (member.getSeat() > roomMember.getSeat()) {
                        roomRedis.editRoom(member);
                        member.setSeat(member.getSeat() - 1);
                        roomMemberDao.update(member);
                        roomRedis.joinRoom(member);//更新redis中的房间用户信息
                    }

                }

                roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
                roomMember.setSeat(-1);
                roomMember.setLeaveTime(new Date());
                roomMemberDao.update(roomMember);

                result.put("roomMembers", roomMembers);
                result.put("userId", user.getId());

                return result;
            } else {
                throw CommonError.ROOM_USER_NOT_IN_ROOM.newException();
            }
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }

    /**
     * 解散房间
     * 未发牌前解散房间是可以直接解散
     * 发牌后解散需要发起解散房间申请,需其他玩家同意
     *
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> dismissRoom(JSONObject data) {
        Map<String, Object> result = new HashMap<>(3);
        Integer type;
        boolean r;
        String uId = (String) data.get("uId");
        Integer roomCode = (Integer) data.get("roomCode");

        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
        Room room = dao.selectOne(roomCriteria);

        if (room != null) {
            if (room.getStart() == Room.start.UNSTART.getCode()) {//未开始游戏,可以直接解散房间
                Set<RoomMember> roomMembers = roomRedis.getRoomMembers(room.getId().toString());
                for (int i = 0; i < roomMembers.size(); i++) {
                    outRoom(data);
                }
                type = 1;
                r = true;
            } else {//已开始游戏,需要申请解散,其他玩家同意才可以解散
                type = 2;
                r = false;
            }
            result.put("room", room);
            result.put("type", type);
            result.put("result", r);
            return result;
        } else {
            throw CommonError.ROOM_NOT_EXIST.newException();
        }

    }

    /**
     * 是否同意解散房间
     *
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> agreeDismiss(JSONObject data) {
        Map<String, Object> result = new HashMap<>(2);
        boolean isAgree = (boolean) data.get("isAgree");
        String uId = (String) data.get("uId");
        Integer roomCode = (Integer) data.get("roomCode");
        Integer type;

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = userDao.selectOne(userCriteria);

        if (user != null) {
            RoomMember roomMember = new RoomMember();
            roomMember.setUserId(user.getId());
            roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);

            Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
            roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
            Room room = dao.selectOne(roomCriteria);
            if (room != null) {

                Set<RoomMember> roomMembers = roomRedis.getRoomMembers(room.getId().toString());

                if (isAgree) {//有玩家同意解散,将该玩家的状态临时改成退出状态
                    roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
                    roomMemberDao.update(roomMember);
                    List<RoomMember> rms = roomMemberDao.selectForDismiss(roomMember);
                    if (rms.size() == Room.playerLimit - 1) {//统计房间中为退出状态玩家的数量,假如数量=3,说明所有玩家都同意解散房间
                        for (RoomMember rm : roomMembers) {
                            Entity.UserCriteria uc = new Entity.UserCriteria();
                            uc.setId(Entity.Value.eq(rm.getUserId()));
                            user = userDao.selectOne(uc);
                            data.put("uId", user.getUId().toString());
                            outRoom(data);
                        }
                        type = 1;//所有玩家同意解散房间,已经解散房间
                    } else {
                        type = 2;//还有玩家没有表态
                        isAgree = false;
                    }

                } else {//有玩家不同意解散,即游戏继续,房间中所有玩家的状态都改成待准备
                    for (RoomMember member : roomMembers) {
                        member.setState(RoomMember.state.UNREADY.getCode());
                        roomMemberDao.update(member);
                    }
                    type = 3;//有玩家反对解散房间,游戏继续
                }
                result.put("type", type);
                result.put("result", isAgree);
                return result;
            } else {
                throw CommonError.ROOM_NOT_EXIST.newException();
            }
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }


    /**
     * 用户准备
     * 需要在房间中并且之前的状态为待准备的玩家才能准备
     * 当四个玩家都已经准备的时候可以开始游戏
     *
     * @return
     */
    @Override
    public Map<String, Object> ready(JSONObject data) throws IllegalAccessException, InstantiationException {
        Map<String, Object> result = new HashMap<>(2);
        Integer type;
        String uId = (String) data.get("uId");

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = userDao.selectOne(userCriteria);

        if (user != null) {
            RoomMember roomMember = new RoomMember();
            roomMember.setUserId(user.getId());
            roomMember.setState(RoomMember.state.UNREADY.getCode());
            roomMember = roomMemberDao.selectByUserIdForReady(roomMember);
            if (roomMember != null) {//需要在房间中并且之前的状态为待准备的玩家才能准备
                roomMember.setState(RoomMember.state.READY.getCode());
                roomMemberDao.update(roomMember);
                //判断房间用户是否已经全部准备
                List<RoomMember> roomMembers = roomMemberDao.selectForStart(roomMember);
                if (roomMembers != null && roomMembers.size() == Room.playerLimit) {//所有玩家都已经准备,可以发牌
                    //调用开始发牌接口
                    Room room = roomDao.selectOne(roomMember.getRoomId());
                    room.setStart(Room.start.STARTED.getCode());
                    Map<String, Object> mahjongGameData = gameService.firstPutOutCard(room, roomMembers);
                    result.putAll(mahjongGameData);
                    roomMember.setState(RoomMember.state.PLAYING.getCode());
                    type = 2;
                } else {
                    type = 1;
                }
                result.put("type", type);
                result.put("roomMember", roomMember);
                return result;
            } else {
                throw CommonError.ROOM_READY_ERROR.newException();
            }
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }


}