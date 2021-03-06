package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RoomDao;
import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.dao.UserDao;
import com.huangmei.commonhm.dao.VoteDao;
import com.huangmei.commonhm.model.*;
import com.huangmei.commonhm.redis.RoomRedis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.CommonUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomServiceImpl extends BaseServiceImpl<Integer, Room> implements RoomService {

    @Autowired
    private GameService gameService;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoomMemberDao roomMemberDao;
    @Autowired
    private VoteDao voteDao;


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
        Integer times = (Integer) data.get("times");
        String uId = (String) data.get("uId");
        Integer multiple = (Integer) data.get("multiple");
        Integer payType = (Integer) data.get("payType");
        Integer type = (Integer) data.get("type");
        Integer diamond = (Integer) data.get("diamond");

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = userDao.selectOne(userCriteria);

        if (user != null) {
            RoomMember roomMember = checkInRoom(user.getId());

            if (roomMember == null) {//玩家没有在房间中

                Room room = new Room();
                room.setCreatedUserId(user.getId());
                room.setType(type);
                room.setPlayers(Room.playerLimit);
                room.setCreatedTime(new Date());
                room.setStart(Room.start.UNSTART.getCode());
                room.setState(Room.state.wait.getCode());
                room.setMultiple(multiple);
                room.setRoomOwnerUid(user.getUId());
                Integer roomCode = CommonUtil.createRoomCode();
                roomCode = checkRoomCode(roomCode);
                room.setRoomCode(roomCode);

                if (multiple >= Room.multiple.COINS_WITH_2000.getCode()) {//创建金币房
                    result = createCoinRoom(room, result);

                } else {//创建好友房
                    if (user.getDiamond() >= (payType == Room.payType.PAY_BY_ONE.getCode() ? diamond : diamond / Room.playerLimit)) {
                        result = createFriendRoom(times, diamond, payType, room, result);
                    } else {
                        throw CommonError.USER_LACK_DIAMONDS.newException();
                    }
                }
                result.put("user", user);
                return result;
            } else {
                throw CommonError.USER_ALREADY_IN_ROOM.newException();
            }
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }
    }

    /**
     * 防止房间号码相同
     *
     * @param roomCode
     * @return
     */
    private Integer checkRoomCode(Integer roomCode) {
        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setState(Entity.Value.ne(Room.state.DISMISS.getCode()));
        roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
        long count = roomDao.selectCount(roomCriteria);
        if (count > 0) {
            roomCode = CommonUtil.createRoomCode();//假如创建房间的号码与未解散的房间相同,需要再次创建,确保不同
            roomCode = checkRoomCode(roomCode);
        }
        return roomCode;
    }

    private Map<String, Object> createCoinRoom(Room room, Map<String, Object> result) {
        roomDao.save(room);
        result.put("room", room);
        RoomMember roomMember = createRoomMember(room, room.getCreatedUserId());
        Set<RoomMember> roomMembers = new HashSet<>();
        roomMembers.add(roomMember);
        result.put("roomMembers", roomMembers);
        roomRedis.createRoom(room, roomMember);
        return result;
    }

    private Map<String, Object> createFriendRoom(Integer times, Integer diamond, Integer payType, Room room, Map<String, Object> result) {
        room.setDiamond(diamond);
        room.setPayType(payType);
        room.setTimes(times);

        roomDao.save(room);
        result.put("room", room);
        RoomMember roomMember = createRoomMember(room, room.getCreatedUserId());
        Set<RoomMember> roomMembers = new HashSet<>();
        roomMembers.add(roomMember);
        result.put("roomMembers", roomMembers);
        roomRedis.createRoom(room, roomMember);

        return result;
    }

    private RoomMember createRoomMember(Room room, Integer userId) {
        RoomMember roomMember = new RoomMember();
        roomMember.setState(RoomMember.state.UNREADY.getCode());
        roomMember.setJoinTime(new Date());
        roomMember.setRoomId(room.getId());
        roomMember.setSeat(1);
        roomMember.setUserId(userId);
        roomMemberDao.save(roomMember);
        return roomMember;

    }

    private Map<String, Object> joinRoomMember(Room room, Long count, User user, Map<String, Object> result) {
        RoomMember roomMember = createRoomMember(room, user.getId());
        roomMember.setSeat((int) (count + 1));
        roomMemberDao.update(roomMember);
        result.put("room", room);
        roomRedis.joinRoom(roomMember);
        //查出房间中所有玩家
        Set<RoomMember> roomMembers = roomRedis.getRoomMembers(roomMember.getRoomId().toString());
        result.put("roomMembers", roomMembers);
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
    public Map<String, Object> joinRoom(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<>(2);

        Integer roomCode = (Integer) data.get("roomCode");
        Integer multiple = (Integer) data.get("multiple");
        Integer roomId = (Integer) data.get("roomId");

        if (user != null) {
            RoomMember roomMember = checkInRoom(user.getId());
            if (roomMember != null) {//玩家没有在房间中
                throw CommonError.USER_ALREADY_IN_ROOM.newException();
            }
            if (roomCode != null) {//进入好友场
                    result = joinFriendRoom(user, roomCode, result);
            } else {//进入金币场

                if (multiple != null) {//进入指定金币场
                    if (user.getCoin() >= multiple) {
                        result = joinCoinRoom(multiple, user, result, roomId);
                        if (result == null) {//所有房间都已经满人,系统自动创建金币房间
                            data.put("uId", user.getUId().toString());
                            data.put("type", Room.type.COINS_ROOM.getCode());
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
                    result = joinCoinRoom(multiple, user, result, roomId);
                    if (result == null) {//所有房间都已经满人或没有对应的金币场房间,系统自动创建金币房间
                        data.put("multiple", multiple);
                        data.put("uId", user.getUId().toString());
                        data.put("type", Room.type.COINS_ROOM.getCode());
                        result = createRoom(data);
                    }
                }
            }

            List<User> users = new ArrayList<>();
            Set<RoomMember> roomMembers = (Set<RoomMember>) result.get("roomMembers");
            if (roomMembers != null) {
                for (RoomMember member : roomMembers) {
                    User u = userDao.selectOne(member.getUserId());
                    users.add(u);
                    if (member.getSeat() == 1) {//说明是房主
                        Room r = (Room) result.get("room");
                        r.setRoomOwnerUid(u.getUId());
                        result.put("room", r);
                    }
                }
                result.put("users", users);
                result.put("userId", user.getId());
            }
            return result;
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }


    }


    private Map<String, Object> joinCoinRoom(Integer multiple, User user, Map<String, Object> result, Integer roomId) {
        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setMultiple(Entity.Value.eq(multiple));
        roomCriteria.setState(Entity.Value.ne(Room.state.DISMISS.getCode()));
        if (roomId != null) {
            roomCriteria.setId(Entity.Value.ne(roomId));
        }
        List<Room> rooms = roomDao.selectList(roomCriteria);
        if (rooms.size() > 0) {
            for (Room r : rooms) {
                Long count = roomRedis.getRoomMemberCount(r.getId().toString());
                if (count < Room.playerLimit) {
                    return joinRoomMember(r, count, user, result);
                }
            }
        }
        return null;
    }

    private Map<String, Object> joinFriendRoom(User user, Integer roomCode, Map<String, Object> result) {
        Room room = getRoomByRoomCode(roomCode);
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
     * @return
     */
    @Override
    public Map<String, Object> outRoom(User user, Room room) {
        room = roomDao.selectOne(room.getId());

        Map<String, Object> result = new HashMap<>(2);
        if (user == null) {
            throw CommonError.USER_NOT_EXIST.newException();
        }
        RoomMember roomMember = checkInRoom(user.getId());
        if (roomMember == null) {
            throw CommonError.USER_NOT_IN_ROOM.newException();
        }
        if (room.getType().equals(Room.type.FRIENDS_ROOM.getCode())
                && room.getCreatedUserId().equals(user.getId())) {
            if (roomMember.getState().equals(RoomMember.state.READY.getCode())) {//房主离开房间之前是准备状态的时候
                roomMember.setState(RoomMember.state.UNREADY.getCode());
                roomMemberDao.update(roomMember);
            }
            Set<RoomMember> roomMembers = roomRedis.getRoomMembers(room.getId().toString());
            List<User> users = new ArrayList<>();
            for (RoomMember member : roomMembers) {
                User u = userDao.selectOne(member.getUserId());
                users.add(u);
            }
            result.put("roomMembers", roomMembers);
            result.put("users", users);
            result.put("result", false);
            return result;
        }

        return outRoom(room, user.getId());
    }

    /**
     * 所有玩家退出房间
     *
     * @param room
     */
    public void outRoom(Room room) {
        RoomMember roomMember = new RoomMember();
        roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
        roomMember.setSeat(-1);
        roomMember.setLeaveTime(new Date());
        Entity.RoomMemberCriteria roomMemberCriteria = new Entity.RoomMemberCriteria();
        roomMemberCriteria.setRoomId(Entity.Value.eq(room.getId()));
        roomMemberCriteria.setLeaveTime(Entity.Value.isNull());
        roomMemberDao.update(roomMember, roomMemberCriteria);

        room.setState(Room.state.DISMISS.getCode());
        room.setLastLoginTime(new Date());
        roomRedis.dismissRoom(room);
        roomDao.update(room);
    }


    /**
     * 单个玩家退出房间
     *
     * @param room
     * @param userId
     * @return
     */
    public Map<String, Object> outRoom(Room room, Integer userId) {
        Map<String, Object> result = new HashMap<>(2);

        RoomMember roomMember = checkInRoom(userId);
        if (roomMember != null) {//需要在房间中才能退出房间
            if (room != null) {
                //金币房游戏开始后不能退出
                if (room.getType().equals(Room.type.COINS_ROOM.getCode()) && room.getState().equals(Room.state.PLAYING.getCode())) {
                    throw CommonError.OUT_ROOM_FAIL.newException();
                }
                Long count = roomRedis.getRoomMemberCount(room.getId().toString());
                if (count > 1) {//房间人数大于1人时,退出房间后房间状态为待开始状态
                    room.setState(Room.state.wait.getCode());
                } else {//房间只剩下1个人时,退出房间后房间状态为解散状态
                    room.setState(Room.state.DISMISS.getCode());
                    room.setLastLoginTime(new Date());
                    roomRedis.dismissRoom(room);
                }
                roomDao.update(room);
                roomRedis.editRoom(roomMember);//移除redis中的房间成员

                //座位顺序比退出玩家位置大的玩家都需要退后一位
                Set<RoomMember> roomMembers = roomRedis.getRoomMembers(room.getId().toString());
                List<RoomMember> roomMemberList = new ArrayList<>(roomMembers);
                Collections.sort(roomMemberList);
                List<User> users = new ArrayList<>();

                for (RoomMember member : roomMemberList) {
                    if (member.getSeat() > roomMember.getSeat()) {
                        roomRedis.editRoom(member);
                        member.setSeat(member.getSeat() - 1);
                        roomMemberDao.update(member);
                        roomRedis.joinRoom(member);//更新redis中的房间用户信息
                    }
                    User u = userDao.selectOne(member.getUserId());
                    users.add(u);
                }

                roomMember.setState(RoomMember.state.OUT_ROOM.getCode());
                roomMember.setSeat(-1);
                roomMember.setLeaveTime(new Date());
                roomMemberDao.update(roomMember);

                result.put("roomMembers", roomMemberList);

                result.put("users", users);
                result.put("result", true);
                return result;
            } else {
                throw CommonError.ROOM_NOT_EXIST.newException();
            }
        } else {
            throw CommonError.USER_NOT_IN_ROOM.newException();
        }
    }

    /**
     * 查询房间信息
     *
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> roomInfo(User user) {
        Map<String, Object> result = new HashMap<>(3);
        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setCreatedUserId(Entity.Value.eq(user.getId()));
        roomCriteria.setState(Entity.Value.ne(Room.state.DISMISS.getCode()));
        Room room = roomDao.selectOne(roomCriteria);

        if (room == null) {
            result.put("result", false);
            return result;
        }

        Long count = roomRedis.getRoomMemberCount(room.getId().toString());

        long time = new Date().getTime() - room.getCreatedTime().getTime();

        result.put("count", count);
        result.put("room", room);
        result.put("time", time);
        return result;

    }

    /**
     * 房主重新加入房间
     *
     * @param user
     * @param room
     * @return
     */
    @Override
    public Map<String, Object> returnRoom(User user, Room room) {
        Map<String, Object> result = new HashMap<>(3);
        Set<RoomMember> roomMembers = roomRedis.getRoomMembers(room.getId().toString());
        List<User> users = new ArrayList<>();
        if (roomMembers != null) {
            for (RoomMember member : roomMembers) {
                User u = userDao.selectOne(member.getUserId());
                users.add(u);
            }
            result.put("users", users);
            result.put("roomMembers", roomMembers);
            result.put("room", room);
        }
        return result;
    }



    /**
     * 申请解散房间
     * 未发牌前解散房间是可以直接解散
     * 发牌后解散需要发起解散房间申请,需其他玩家同意
     *
     * @return
     */
    @Override
    public Map<String, Object> dismissRoom(Room room, User user) {
        Map<String, Object> result = new HashMap<>(3);
        boolean r;
        room = roomDao.selectOne(room.getId());
        if (room != null) {

            if (room.getStart() == Room.start.UNSTART.getCode()) {//未开始游戏,可以直接解散房间
                outRoom(room);
                r = true;
            } else {//已开始游戏,需要申请解散,其他玩家同意才可以解散

                //发起投票两分钟内不能再次投票
                List<Vote> votes = checkVote(room.getId());
                if (votes.size() > 0) {//距离上一次投票时间不够两分钟不能再次发起投票
                    throw CommonError.VOTE_APPLY_FAIL.newException();
                }
                Set<RoomMember> roomMembers = roomRedis.getRoomMembers(room.getId().toString());
                r = false;
                for (RoomMember roomMember : roomMembers) {//创建四条投票记录
                    Vote vote = new Vote();
                    vote.setRoomId(room.getId());
                    vote.setType(Vote.type.DISMISS_VOTE.getCode());
                    vote.setState(Vote.state.UN_VOTE.getCode());
                    vote.setOrganizerUserId(user.getId());
                    vote.setVoterUserId(roomMember.getUserId());
                    vote.setStatus(Vote.status.PROCESSING.getCode());
                    if (roomMember.getUserId().equals(user.getId())) {//假如是发起人,则默认投票为同意
                        vote.setState(Vote.state.AGREE.getCode());
                    }
                    voteDao.save(vote);

                }
            }
            result.put("uId", user.getUId());
            result.put("nickName", user.getNickName());
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
    public Map<String, Object> agreeDismiss(JSONObject data, User user, Room room) {
        Map<String, Object> result = new HashMap<>(2);
        boolean isAgree = (boolean) data.get("isAgree");

        if (user == null) {
            throw CommonError.USER_NOT_EXIST.newException();
        }
        RoomMember roomMember = checkInRoom(user.getId());

        if (roomMember == null) {
            throw CommonError.USER_NOT_IN_ROOM.newException();
        }

        if (room == null) {
            throw CommonError.ROOM_NOT_EXIST.newException();
        }

        Entity.VoteCriteria voteCriteria = new Entity.VoteCriteria();
        voteCriteria.setVoterUserId(Entity.Value.eq(user.getId()));
        voteCriteria.setRoomId(Entity.Value.eq(room.getId()));
        voteCriteria.setType(Entity.Value.eq(Vote.type.DISMISS_VOTE.getCode()));
        voteCriteria.setStatus(Entity.Value.eq(Vote.status.PROCESSING.getCode()));
        Vote vote = voteDao.selectOne(voteCriteria);

        if (isAgree) {//有玩家同意解散
            vote.setState(Vote.state.AGREE.getCode());
            voteDao.update(vote);

            long count = checkForVote(room.getId());

            if (count == 4) {//全部人投票同意解散房间
                outRoom(room);
            }

        } else {//有玩家不同意解散,即游戏继续
            vote.setState(Vote.state.DISAGREE.getCode());
            voteDao.update(vote);
        }

        result.put("result", isAgree);
        result.put("uId", user.getUId());
        result.put("nickName", user.getNickName());
        return result;


    }

    /**
     * 获取的金币房在玩人数
     *
     * @return
     */
    @Override
    public Map<String, Object> numberOfPlayers() {
        Map<String, Object> result = new HashMap<>(2);
        long primary = roomDao.countForPlayers(Room.multiple.COINS_WITH_2000.getCode());
        long senior = roomDao.countForPlayers(Room.multiple.COINS_WITH_20000.getCode());
        result.put("primary", primary);
        result.put("senior", senior);
        return result;
    }


    /**
     * 用户准备
     * 需要在房间中并且之前的状态为待准备的玩家才能准备
     * 当四个玩家都已经准备的时候可以开始游戏
     *
     * @return
     */
    @Override
    public Map<String, Object> ready(User user) throws IllegalAccessException, InstantiationException {
        Map<String, Object> result = new HashMap<>(4);
        Integer type;

        if (user == null) {
            throw CommonError.USER_NOT_EXIST.newException();
        }

        RoomMember roomMember = new RoomMember();
        roomMember.setUserId(user.getId());
        roomMember.setState(RoomMember.state.UNREADY.getCode());
        roomMember = roomMemberDao.selectByUserIdForReady(roomMember);
        if (roomMember == null) {//需要在房间中并且之前的状态为待准备的玩家才能准备
            throw CommonError.READY_ERROR.newException();
        }

        roomMember.setState(RoomMember.state.READY.getCode());
        roomMemberDao.update(roomMember);

        //判断房间用户是否已经全部准备
        List<RoomMember> roomMembers = roomMemberDao.selectForStart(roomMember);
        if (roomMembers != null && roomMembers.size() == Room.playerLimit) {//所有玩家都已经准备,可以发牌
            //调用开始发牌接口
            Room room = roomDao.selectOne(roomMember.getRoomId());
            room.setStart(Room.start.STARTED.getCode());
            room.setState(Room.state.PLAYING.getCode());
            roomDao.update(room);
            roomRedis.updateRoom(room);
            Map<String, Object> mahjongGameData = gameService.putOutHandCard(room, roomMembers);
            result.putAll(mahjongGameData);
            type = 2;
        } else {
            type = 1;
        }
        roomRedis.editRoom(roomMember);
        roomRedis.joinRoom(roomMember);
        result.put("type", type);
        result.put("roomId", roomMember.getRoomId());
        result.put("uId", user.getUId());
        result.put("state", roomMember.getState());
        return result;
    }

    /**
     * 聊天模块
     *
     * @param data
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> communication(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(3);
        Integer type = (Integer) data.get("type");
        Object content = data.get("content");

        RoomMember roomMember = checkInRoom(user.getId());
        if (roomMember != null) {
            result.put("roomId", roomMember.getRoomId());
            result.put("type", type);
            result.put("content", content);
            return result;

        } else {
            throw CommonError.USER_NOT_IN_ROOM.newException();
        }


    }

    /**
     * 投票超时后执行默认同意
     *
     * @param roomId
     * @return
     */
    @Override
    public void dismissRoomVoteTask(Integer roomId) {

        Entity.VoteCriteria voteCriteria = new Entity.VoteCriteria();
        voteCriteria.setRoomId(Entity.Value.eq(roomId));
        voteCriteria.setState(Entity.Value.eq(Vote.state.UN_VOTE.getCode()));
        List<Vote> votes = voteDao.selectList(voteCriteria);
        if (votes.size() > 0) {//还有玩家没有投票
            for (Vote vote : votes) {
                vote.setState(Vote.state.AGREE.getCode());
                voteDao.update(vote);
            }
            long count = checkForVote(roomId);

            if (count == 4) {//全部人投票同意解散房间
                Room room = roomDao.selectOne(roomId);
                outRoom(room);
            }

        }

        //超过两分钟后这一轮投票设置为已结束
        List<Vote> vs = checkVote(roomId);
        for (Vote v : vs) {
            v.setStatus(Vote.status.FINISH.getCode());
            voteDao.update(v);
        }


    }


    /**
     * 根据房间号查询房间
     *
     * @param roomCode
     * @return
     */
    private Room getRoomByRoomCode(Integer roomCode) {
        Entity.RoomCriteria roomCriteria = new Entity.RoomCriteria();
        roomCriteria.setRoomCode(Entity.Value.eq(roomCode));
        roomCriteria.setState(Entity.Value.ne(Room.state.DISMISS.getCode()));
        Room room = roomDao.selectOne(roomCriteria);
        return room;
    }


    /**
     * 判断玩家是否在房间中
     *
     * @param userId
     * @return
     */
    private RoomMember checkInRoom(Integer userId) {
        Entity.RoomMemberCriteria roomMemberCriteria = new Entity.RoomMemberCriteria();
        roomMemberCriteria.setUserId(Entity.Value.eq(userId));
        roomMemberCriteria.setLeaveTime(Entity.Value.isNull());
        return roomMemberDao.selectOne(roomMemberCriteria);
    }

    /**
     * 判断房间中是否还有投票进行中的玩家
     *
     * @param roomId
     * @return
     */
    private List<Vote> checkVote(Integer roomId) {
        Entity.VoteCriteria voteCriteria = new Entity.VoteCriteria();
        voteCriteria.setRoomId(Entity.Value.eq(roomId));
        voteCriteria.setType(Entity.Value.eq(Vote.type.DISMISS_VOTE.getCode()));
        voteCriteria.setStatus(Entity.Value.eq(Vote.status.PROCESSING.getCode()));
        List<Vote> vs = voteDao.selectList(voteCriteria);
        return vs;
    }

    /**
     * 统计这一轮投同意玩家的数量
     *
     * @param roomId
     * @return
     */
    public long checkForVote(Integer roomId) {
        Entity.VoteCriteria vc = new Entity.VoteCriteria();
        vc.setRoomId(Entity.Value.eq(roomId));
        vc.setState(Entity.Value.eq(Vote.state.AGREE.getCode()));
        vc.setStatus(Entity.Value.eq(Vote.status.PROCESSING.getCode()));
        long count = voteDao.selectCount(vc);
        return count;
    }


}