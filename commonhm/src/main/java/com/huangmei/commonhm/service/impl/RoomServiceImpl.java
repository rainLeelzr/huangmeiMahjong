package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RoomDao;
import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.dao.UserDao;
import com.huangmei.commonhm.model.Entity;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.redis.RoomRedis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.CommonUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomServiceImpl extends BaseServiceImpl<Integer, Room> implements RoomService {

    @Autowired
    private RoomDao dao;

    @Autowired
    private UserDao userDao;
    @Autowired
    private RoomMemberDao roomMemberDao;

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

        Map<String, Object> result = new HashMap<String, Object>(2);
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
        roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
        roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);
        if (roomMember == null) {//玩家没有在房间中
            if (user != null) {
                Room room = new Room();
                room.setCreatedUserId(user.getId());
                room.setType(type);
                room.setPlayers(Room.playerLimit);
                room.setCreatedTime(new Date());
                room.setState(Room.state.wait.getCode());
                room.setRoomCode(Integer.parseInt(CommonUtil.createRandomNumeric(4)));
                if (multiple != null) {//创建金币房
                    result = creatCoinRoom(multiple, room, result);
                } else {//创建好友房
                    if (user.getDiamond() >= (payType == Room.payType.PAY_BY_ONE.getCode() ? diamond : diamond / Room.playerLimit)) {
                        result = creatFriendRoom(user, times, diamond, payType, room, result);
                    } else {
                        throw CommonError.USER_LACK_DIAMONDS.newException();
                    }
                }
                return result;
            } else {
                throw CommonError.USER_NOT_EXIST.newException();
            }
        } else {
            throw CommonError.ROOM_USER_IN_ROOM.newException();
        }
    }

    private Map<String, Object> creatCoinRoom(Integer multiple, Room room, Map<String, Object> result) {
        room.setMultiple(multiple);
        dao.save(room);
        result.put("room", room);
        RoomMember roomMember = creatRoomMember(room, room.getCreatedUserId());
        result.put("roomMember", roomMember);
        roomRedis.createRoom(room, roomMember);
        return result;
    }

    private Map<String, Object> creatFriendRoom(User user, String times, Integer diamond, Integer payType, Room room, Map<String, Object> result) {
        room.setDiamond(diamond);
        room.setPayType(payType);
        room.setTimes(times);
        dao.save(room);
        result.put("room", room);
        RoomMember roomMember = creatRoomMember(room, room.getCreatedUserId());
        result.put("roomMember", roomMember);
        roomRedis.createRoom(room, roomMember);

        userDao.update(user);
        return result;
    }

    private RoomMember creatRoomMember(Room room, Integer createdUserId) {
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
        RoomMember roomMember = creatRoomMember(room, user.getId());
        roomMember.setSeat((int) (count + 1));
        roomMemberDao.update(roomMember);
        result.put("roomMember", roomMember);
        result.put("room", room);
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
        roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
        roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);
        if (roomMember == null) {//玩家没有在房间中
            if (user != null) {
                if (roomCode != null) {//进入好友场
                    result = joinFriendRoom(user, roomCode, result);
                } else {//进入金币场
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
                return result;
            } else {
                throw CommonError.USER_NOT_EXIST.newException();
            }
        } else {
            throw CommonError.ROOM_USER_IN_ROOM.newException();
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
            roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
            roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);
            if (roomMember != null) {//需要在房间中才能退出房间
                Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
                roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
                Room room = dao.selectOne(roomCriteria);

                if (room != null) {//只要有用户退出了房间房间状态一定为待开始状态
                    room.setState(Room.state.wait.getCode());
                    dao.update(room);
                } else {
                    throw CommonError.ROOM_NOT_EXIST.newException();
                }

                roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
                roomMember.setSeat(-1);
                roomMember.setLeaveTime(new Date());
                roomMemberDao.update(roomMember);

                result.put("roomMember", roomMember);
                result.put("userId", user.getId());

                roomRedis.editRoom(roomMember);
                return result;
            } else {
                throw CommonError.ROOM_USER_NOT_IN_ROOM.newException();
            }
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }

    /**
     * 用户准备
     * 需要在房间中并且之前的状态为待准备的玩家才能准备
     * 当四个玩家都已经准备的时候才可以开始游戏
     *
     * @return
     */
    @Override
    public RoomMember ready(JSONObject data) {
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
                return roomMember;
            } else {
                throw CommonError.ROOM_READY_ERROR.newException();
            }
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }

    /**
     * 开始游戏
     * 需要四个玩家都准备才可以开始
     *
     * @param data
     * @return
     */
    @Override
    public RoomMember startGame(JSONObject data) {
        String roomCode = (String) data.get("roomCode");

        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
        Room room = dao.selectOne(roomCriteria);

        RoomMember roomMember = new RoomMember();
        roomMember.setState(RoomMember.state.READY.getCode());
        roomMember.setRoomId(room.getId());
        List<RoomMember> roomMembers = roomMemberDao.selectForStart(roomMember);
        if (roomMembers != null && roomMembers.size() == Room.playerLimit) {//所有玩家都已经准备
            //调用开始游戏接口,
            room.setState(Room.state.PLAYING.getCode());
            dao.update(room);
            return null;

        } else {
            throw CommonError.ROOM_UNREADY.newException();
        }
    }

}