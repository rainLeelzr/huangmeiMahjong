package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.*;
import com.huangmei.commonhm.model.*;
import com.huangmei.commonhm.model.mahjong.*;
import com.huangmei.commonhm.model.mahjong.vo.GameStartVo;
import com.huangmei.commonhm.model.mahjong.vo.GangVo;
import com.huangmei.commonhm.model.mahjong.vo.PersonalCardVo;
import com.huangmei.commonhm.model.mahjong.vo.ReconnectionVo;
import com.huangmei.commonhm.model.vo.ScoreVo;
import com.huangmei.commonhm.model.vo.UserVo;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.service.UserService;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.CommonUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.util.*;

@Service
public class UserServiceImpl extends BaseServiceImpl<Integer, User> implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;
    @Autowired
    private RoomMemberDao roomMemberDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private TranRecordDao tranRecordDao;
    @Autowired
    private NoticeDao noticeDao;

    @Autowired
    private RoomService roomService;

    @Autowired
    private GameRedis gameRedis;

    /**
     * 用户登录
     *
     * @param data
     * @param ip
     * @return
     * @throws Exception
     */
    public Map<String, Object> login(JSONObject data, String ip) throws Exception {

        Map<String, Object> result = new HashMap<String, Object>(2);
        //1正常登陆,2进入了房间，未准备或已准备，4进入了房间，游戏中，5结算后未显示结算页面
        Integer loginType;

        String openId = (String) data.get("openId");
        String nickName = (String) data.get("nickName");
        String image = (String) data.get("image");
        Integer sex = data.getInt("sex");

        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setOpenId(Entity.Value.eq(openId));
        User user = userDao.selectOne(userCriteria);


        if (user == null) {//新用户登录
            user = new User();
            user.setImage(image);
            user.setIp(ip);
            user.setLastLoginTime(new Date());
            user.setNickName(nickName);
            user.setSex(sex);
            user.setOpenId(openId);
            user.setCoin(30000);
            // DEBUGING 玩家钻石
            user.setDiamond(1000);
            user.setHorn(5);
            Integer uId = CommonUtil.createUserCode();
            uId = checkUId(uId);
            user.setUId(uId);
            userDao.save(user);
            loginType = 1;
        } else {
            if (!image.equals(user.getImage())) {//头像发生变化
                user.setImage(image);
            } else if (!nickName.equals(user.getNickName())) {//昵称发生变化
                user.setNickName(nickName);
            }
            user.setLastLoginTime(new Date());
            userDao.update(user);

            RoomMember roomMember = checkInRoom(user.getId());
            if (roomMember != null) {
                Room room = roomService.selectOne(roomMember.getRoomId());
                result.put("room", room);

                if (roomMember.getState().equals(RoomMember.state.PLAYING.getCode())) {
                    loginType = 4;// 进入了房间，游戏中

                    // 组装游戏信息，返回给客户端渲染界面
                    ReconnectionVo reconnectionVo = genReconnectionVo4Playing(room);
                    result.put("gameData", reconnectionVo);

                } else if (roomMember.getState().equals(RoomMember.state.UNREADY.getCode())
                        || roomMember.getState().equals(RoomMember.state.READY.getCode())) {
                    loginType = 2;
                    log.warn(
                            "用户登录时，数据库中含有roomMember.state={}[{}],将其踢出房间。",
                            RoomMember.state.UNREADY.getCode(),
                            RoomMember.state.UNREADY.getName()
                    );
                    // DEBUGING　玩家未点击准备,或已准备，但游戏未开始时，踢出房间
                    // roomService.outRoom(room.getRoomCode(), user.getId());
                } else {
                    throw CommonError.SYS_PARAM_ERROR.newException();
                }
            } else {
                loginType = 1;
            }
        }
        result.put("user", user);
        result.put("login_type", loginType);
        return result;
    }

    /**
     * 生成正在游戏中的数据
     */
    private ReconnectionVo genReconnectionVo4Playing(Room room) {
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        GameStartVo gameStart = new GameStartVo();
        gameStart.setDices(mahjongGameData.getDices());
        gameStart.setBaoMotherId(mahjongGameData.getBaoMother().getId());
        gameStart.setBaoMahjongs(Mahjong.parseToIds(mahjongGameData.getBaoMahjongs()));
        gameStart.setCurrentTimes(mahjongGameData.getCurrentTimes());

        Entity.RoomMemberCriteria roomMemberCriteria = new Entity.RoomMemberCriteria();
        roomMemberCriteria.setRoomId(Entity.Value.eq(room.getId()));

        // 打出的麻将，需要排除碰了和杠了的麻将
        List<OutCard> outMahjongs = new ArrayList<>(mahjongGameData.getOutCards());

        // roomMembers的User在api中设置
        List<RoomMember> roomMembers = roomMemberDao.selectList(roomMemberCriteria);
        for (RoomMember roomMember : roomMembers) {
            PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(
                    mahjongGameData.getPersonalCardInfos(),
                    roomMember.getUserId()
            );

            // 去掉outMahjong列表中碰了的麻将
            for (Combo combo : personalCardInfo.getPengs()) {
                OutCard.filterOutCard(outMahjongs, combo.getMahjongs());
            }

            // 去掉outMahjong列表中杠了的麻将
            List<Combo> gangs = personalCardInfo.getGangs();
            for (Combo combo : gangs) {
                OutCard.filterOutCard(outMahjongs, combo.getMahjongs());
            }

            PersonalCardVo pCardVo = new PersonalCardVo(
                    Mahjong.parseToIds(personalCardInfo.getHandCards()),
                    Mahjong.parseCombosToMahjongIds(personalCardInfo.getPengs()),
                    GangVo.parseFromGangCombos(personalCardInfo.getGangs()),
                    OutCard.filterByUserId(outMahjongs, roomMember.getUserId())
            );
            roomMember.setPersonalCardVo(pCardVo);

            // 设置庄家
            if (personalCardInfo.getRoomMember().getSeat().equals(mahjongGameData.getBankerSite())) {
                gameStart.setBankerUId(personalCardInfo.getRoomMember().getUserId());// uId在api层再设置
            }
        }

        ReconnectionVo reconnectionVo = new ReconnectionVo(
                roomMembers,
                room,
                gameStart,
                mahjongGameData.getLeftCards().size()
        );

        return reconnectionVo;
    }

    /**
     * 防止uId重复
     *
     * @param uId
     * @return
     */
    private Integer checkUId(Integer uId) {
        Entity.UserCriteria uc = new Entity.UserCriteria();
        uc.setUId(Entity.Value.eq(uId));
        long count = userDao.selectCount(uc);
        if (count > 0) {
            uId = CommonUtil.createUserCode();
            uId = checkUId(uId);
        }
        return uId;
    }

    public TextMessage TestConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 登出
     *
     * @param data
     * @return
     */
    @Override
    public User logout(JSONObject data) {
        String uId = (String) data.get("uId");
        Entity.UserCriteria userCriteria = new Entity.UserCriteria();
        userCriteria.setUId(Entity.Value.eq(uId));
        User user = userDao.selectOne(userCriteria);
        if (user != null) {
            return user;
        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }

    /**
     * 获取用户信息
     *
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> getUser(User user) {
        Map<String, Object> result = new HashMap<String, Object>(6);
        if (user != null) {
            result.put("user", user);
            Integer huType = scoreDao.selectBestHuType(user.getId());
            if (huType != null) {
                for (Score.HuType value : Score.HuType.values()) {
                    if (value.getId().equals(huType)) {
                        result.put("bestHuType", value.getName());
                        break;
                    }
                }
            } else {
                result.put("bestHuType", "");
            }

            Entity.ScoreCriteria scoreCriteria = new Entity.ScoreCriteria();
            scoreCriteria.setUserId(Entity.Value.eq(user.getId()));
            long count = scoreDao.selectCount(scoreCriteria);//总局数
            scoreCriteria.setWinType(Entity.Value.ne(2));
            scoreCriteria.setWinType(Entity.Value.ne(4));
            long lose_count = scoreDao.selectCount(scoreCriteria);//输局总数

            result.put("lose", lose_count);
            result.put("win", count - lose_count);
            return result;

        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }

    /**
     * 抽奖
     *
     * @param data
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> prizeDraw(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(3);
        Integer way = TranRecord.way.DRAW_BY_FREE.getCode();
        String query = (String) data.get("query");

        TranRecord tr = new TranRecord();
        tr.setWay(way);
        tr.setUserId(user.getId());
        Long count = tranRecordDao.countForPrizeDraw(tr);

        if (query != null) {//客户端需要判断是否免费抽奖
            if (count < 1) {
                result.put("free", true);
            } else {
                result.put("free", false);
            }
        } else {
            if (count < 1) {//说明当日还没有进行免费抽奖
                result = PrizeRandom(user, result, way);
            } else {
                way = TranRecord.way.DRAW_BY_COINS.getCode();
                tr.setWay(way);
                count = tranRecordDao.countForPrizeDraw(tr);
                if (count < 3) {//说明当日还可以用金币进行抽奖
                    if (user.getCoin() >= 10000) {
                        user.setCoin(user.getCoin() - 10000);
                        result = PrizeRandom(user, result, way);

                    } else {
                        throw CommonError.USER_LACK_COINS.newException();
                    }
                } else {
                    throw CommonError.ALREADY_DRAW_COINS.newException();
                }
            }
            result.put("way", way);
        }

        return result;

    }


    /**
     * 抽奖实现
     *
     * @param user
     * @param result
     * @param way
     * @return
     */
    private Map<String, Object> PrizeRandom(User user, Map<String, Object> result, Integer way) {
        int random = -1;
        List<Prize> prizes = packPrize();

        try {
            //计算总权重
            double sumWeight = 0;
            for (Prize p : prizes) {
                sumWeight += p.getPrize_weight();
            }

            //产生随机数
            double randomNumber;
            randomNumber = Math.random();

            //根据随机数在所有奖品分布的区域并确定所抽奖品
            double d1 = 0;
            double d2 = 0;
            for (int i = 0; i < prizes.size(); i++) {
                d2 += prizes.get(i).getPrize_weight() / sumWeight;
                if (i == 0) {
                    d1 = 0;
                } else {
                    d1 += prizes.get(i - 1).getPrize_weight() / sumWeight;
                }
                if (randomNumber >= d1 && randomNumber <= d2) {
                    random = i;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("生成抽奖随机数出错，出错原因：" + e.getMessage());
        }

        //抽完奖创建一条交易记录
        String prizeName = prizes.get(random).getPrize_name();
        switch (prizeName) {
            case "100钻石":
                user.setDiamond(user.getDiamond() + 100);
                createRecord(user, way, TranRecord.itemType.DIAMOND.getCode(), 100);
                break;
            case "50钻石":
                user.setDiamond(user.getDiamond() + 50);
                createRecord(user, way, TranRecord.itemType.DIAMOND.getCode(), 50);
                break;
            case "10钻石":
                user.setDiamond(user.getDiamond() + 10);
                createRecord(user, way, TranRecord.itemType.DIAMOND.getCode(), 10);
                break;
            case "5钻石":
                user.setDiamond(user.getDiamond() + 5);
                createRecord(user, way, TranRecord.itemType.DIAMOND.getCode(), 5);
                break;
            case "50000金币":
                user.setCoin(user.getCoin() + 50000);
                createRecord(user, way, TranRecord.itemType.COIN.getCode(), 50000);
                break;
            case "2钻石":
                user.setDiamond(user.getDiamond() + 2);
                createRecord(user, way, TranRecord.itemType.DIAMOND.getCode(), 2);
                break;
            case "2000金币":
                user.setCoin(user.getCoin() + 2000);
                createRecord(user, way, TranRecord.itemType.COIN.getCode(), 2000);
                break;
        }
        userDao.update(user);

        result.put("user", user);
        result.put("prize", prizeName);
        return result;
    }

    /**
     * 封装奖品
     *
     * @return
     */
    private List<Prize> packPrize() {
        List<Prize> prizes = new ArrayList<Prize>();
        Prize p1 = new Prize("100钻石", 0.01);
        Prize p2 = new Prize("50钻石", 0.1);
        Prize p3 = new Prize("10钻石", 1.0);
        Prize p4 = new Prize("5钻石", 5.0);
        Prize p5 = new Prize("50000金币", 10.0);
        Prize p6 = new Prize("2钻石", 10.0);
        Prize p7 = new Prize("2000金币", 73.89);
        prizes.add(p1);
        prizes.add(p2);
        prizes.add(p3);
        prizes.add(p4);
        prizes.add(p5);
        prizes.add(p6);
        prizes.add(p7);
        return prizes;
    }

    /**
     * 创建一条交易记录
     *
     * @return
     */
    private void createRecord(User user, Integer way, Integer itemType, Integer quantity) {
        TranRecord tranRecord = new TranRecord();
        tranRecord.setUserId(user.getId());
        tranRecord.setWay(way);
        tranRecord.setTranTimes(new Date());
        tranRecord.setQuantity(quantity);
        tranRecord.setItemType(itemType);
        tranRecordDao.save(tranRecord);

    }

    /**
     * 免费领取金币
     *
     * @param user
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> freeCoins(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        Integer way = TranRecord.way.FREE_COIN.getCode();
        String query = (String) data.get("query");

        TranRecord tr = new TranRecord();
        tr.setWay(way);
        tr.setUserId(user.getId());
        Long count = tranRecordDao.countForPrizeDraw(tr);

        if (query != null) {//表示查询当前用户的领取情况
            if (count > 0) {
                result.put("count", count);
                TranRecord tranRecord = tranRecordDao.selectRecent(tr);
                result.put("lastDrawTime", tranRecord.getTranTimes());
                result.put("now", new Date());
            }
        } else {//领取金币
            if (count > 0) {//表示今天有领取记录
                TranRecord tranRecord = tranRecordDao.selectRecent(tr);
                //每两小时领取一次,需要计时
                if (new Date().getTime() - tranRecord.getTranTimes().getTime() >= 3600 * 2 * 1000) {
                    result = getCoins(user, result, way, count);
                } else {
                    throw CommonError.NOT_ABLE_GET_COINS.newException();
                }
            } else {//当天第一次领取
                result = getCoins(user, result, way, count);
            }
            result.put("count", count + 1);
        }
        return result;
    }

    /**
     * 领取金币
     *
     * @param user
     * @param result
     * @param way
     * @param count
     */
    private Map<String, Object> getCoins(User user, Map<String, Object> result, Integer way, Long count) {
        if (count < 5) {//满足领取金币的资格
            if (count < 4) {
                user.setCoin(user.getCoin() + 2000);
            } else {//当天第5次
                user.setCoin(user.getCoin() + 2000);
                user.setDiamond(user.getDiamond() + 1);
                createRecord(user, way, TranRecord.itemType.DIAMOND.getCode(), 1);
            }
            userDao.update(user);
            //交易记录
            createRecord(user, way, TranRecord.itemType.COIN.getCode(), 2000);
            result.put("user", user);
            result.put("count", count + 1);
            return result;
        } else {//不满足领取金币的资格
            throw CommonError.ALREADY_GET_COINS.newException();
        }
    }

    /**
     * 购买金币/钻石/道具
     *
     * @param data
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> buy(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);

        Integer coin = (Integer) data.get("coin");
        Integer diamond = (Integer) data.get("diamond");
        Integer horn = (Integer) data.get("horn");

        // todome 支付接口对接

        if (coin != null) {
            user.setCoin(user.getCoin() + coin);
        } else if (diamond != null) {
            user.setDiamond(user.getDiamond() + diamond);
        } else if (horn != null) {
            user.setHorn(user.getHorn() + horn);
        }
        userDao.update(user);
        result.put("user", user);
        return result;
    }

    /**
     * 绑定手机
     *
     * @param data
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> bindPhone(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        String phone = (String) data.get("phone");
        String query = (String) data.get("query");

        Integer way = TranRecord.way.BIND_PHONE.getCode();
        Entity.TranRecordCriteria tranRecordCriteria = new Entity.TranRecordCriteria();
        tranRecordCriteria.setUserId(Entity.Value.eq(user.getId()));
        tranRecordCriteria.setWay(Entity.Value.eq(way));
        long count = tranRecordDao.selectCount(tranRecordCriteria);

        if (query != null) {//检查用户是否已经领取绑定手机钻石
            if (count < 1) {
                result.put("receive", false);
            } else {
                result.put("receive", true);
            }
        } else {
            if (phone != null) {//绑定手机
                if (user.getMobilePhone() == null) {//还没有绑定过手机
                    user.setMobilePhone(phone);
                } else {
                    throw CommonError.ALREADY_BIND_PHONE.newException();
                }
            } else {//领取钻石
                if (user.getMobilePhone() != null) {//已经绑定手机

                    if (count < 1) {//没有领取过绑定手机钻石
                        user.setDiamond(user.getDiamond() + 2);
                        //生成交易记录
                        createRecord(user, way, TranRecord.itemType.DIAMOND.getCode(), 2);
                    } else {
                        throw CommonError.ALREADY_GET_DIAMOND.newException();
                    }
                } else {
                    throw CommonError.UN_BIND_PHONE.newException();
                }
            }
            userDao.update(user);
            result.put("user", user);
        }
        return result;

    }

    /**
     * 每日任务胜利十局可以领取2000金币
     *
     * @param user
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> tenWins(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        String query = (String) data.get("query");

        TranRecord tr = new TranRecord();
        tr.setWay(TranRecord.way.WIN.getCode());
        tr.setUserId(user.getId());
        Long num = tranRecordDao.countForPrizeDraw(tr);

        tr.setWay(TranRecord.way.TEN_WINS.getCode());
        Long count = tranRecordDao.countForPrizeDraw(tr);

        if (query != null) {//查询当前胜利局数
            result.put("winNum", num);
            if (count < 1) {
                result.put("receive", false);
            } else {
                result.put("receive", true);
            }
        } else {
            if (num >= 10) {//当天胜利局数大于十局

                if (count < 1) {//当天还没有领取胜利任务金币
                    user.setCoin(user.getCoin() + 2000);
                    userDao.update(user);
                    createRecord(user, TranRecord.way.TEN_WINS.getCode(), TranRecord.itemType.COIN.getCode(), 2000);
                    result.put("user", user);
                } else {
                    throw CommonError.ALREADY_GET_COINS.newException();
                }
            } else {
                throw CommonError.NOT_ENOUGH_GAMES.newException();
            }
        }
        return result;
    }

    /**
     * 获取房间战绩
     *
     * @param room
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> getStanding(Room room, User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        List<ScoreVo> stands = new ArrayList();

        if (room != null) {

            Entity.ScoreCriteria scoreCriteria = new Entity.ScoreCriteria();
            scoreCriteria.setUserId(Entity.Value.eq(user.getId()));
            scoreCriteria.setRoomId(Entity.Value.eq(room.getId()));
            List<Score> scores = scoreDao.selectList(scoreCriteria);
            if (scores.size() > 0) {

                for (Score score : scores) {//个人在这个房间的所有战绩
                    ScoreVo sv = new ScoreVo();
                    List<UserVo> players = new ArrayList();
                    sv.setCreatedTime(score.getCreatedTime());
                    sv.setRoomCode(room.getRoomCode());

                    if (score.getWinType().equals(Score.WinType.ZI_MO.getId())
                            || score.getWinType().equals(Score.WinType.JIE_PAO.getId())) {
                        sv.setState(ScoreVo.WIN);
                    } else {
                        sv.setState(ScoreVo.LOSE);
                    }

                    Entity.ScoreCriteria sc = new Entity.ScoreCriteria();
                    sc.setTimes(Entity.Value.eq(score.getTimes()));
                    sc.setRoomId(Entity.Value.eq(room.getId()));
                    List<Score> scs = scoreDao.selectList(sc);
                    for (Score se : scs) {//其中每局四个玩家的战绩
                        User u = userDao.selectOne(se.getUserId());
                        UserVo userVo = new UserVo();
                        userVo.setNickName(u.getNickName());
                        userVo.setuId(u.getUId());
                        if (se.getWinType().equals(Score.WinType.ZI_MO.getId())
                                || se.getWinType().equals(Score.WinType.JIE_PAO.getId())) {
                            userVo.setScore(se.getScore());
                        } else {
                            userVo.setScore(-se.getScore());
                        }
                        players.add(userVo);
                    }
                    sv.setPlayers(players);
                    stands.add(sv);
                }
                result.put("stands", stands);
                return result;
            } else {
                throw CommonError.NOT_STANDS.newException();
            }
        } else {
            throw CommonError.USER_NOT_IN_ROOM.newException();
        }
    }

    /**
     * 使用喇叭全服喊话
     *
     * @param data
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> hornSpeak(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        List<String> strArrays = new ArrayList();
        String query = (String) data.get("query");
        String userMsg = (String) data.get("userMsg");
        if (query != null) {//查询滚动公告
            //todome   查询系统滚动公告
            result.put("administrator", "等待管理后台对接");
            //查询用户内容
            List<Notice> notices = noticeDao.selectAll();
            if (notices.size() > 0) {
                for (Notice notice : notices) {
                    if (new Date().getTime() - notice.getCreateTime().getTime() < 600 * 1000) {//10分钟内需要继续滚动喊话
                        User u = userDao.selectOne(notice.getUserId());
                        String temp = u.getNickName() + ":" + notice.getContent();
                        strArrays.add(temp);
                    } else {
                        noticeDao.delete(notice.getId());
                    }
                }
            }
            result.put("userMsg", strArrays);
        } else {//全服喊话
            if (user.getHorn() >= 1) {
                user.setHorn(user.getHorn() - 1);
                userDao.update(user);
                //储存用户喊话内容
                Notice notice = new Notice();
                notice.setUserId(user.getId());
                notice.setContent(userMsg);
                notice.setCreateTime(new Date());
                noticeDao.save(notice);

                result.put("user", user);
                result.put("userMsg", user.getNickName() + ":" + userMsg);
            } else {
                throw CommonError.USER_LACK_HORNS.newException();
            }
        }
        return result;

    }

    /**
     * 获取公告栏
     *
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> systemNotice(User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        //todome 查询公告栏
        result.put("image1", "xxxxxxxxxxxxxxxxxxx");
        result.put("text2", "等待管理后台对接");
        result.put("text3", "等待管理后台对接");
        return result;
    }

    /**
     * 绑定推广码
     *
     * @param data
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> bindPromoteCode(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        Integer code = (Integer) data.get("code");

        if (user.getPromoterId() == null) {//未绑定过推广码
            Entity.UserCriteria userCriteria = new Entity.UserCriteria();
            userCriteria.setPromoteCode(Entity.Value.eq(code));
            User promoter = userDao.selectOne(userCriteria);
            if (promoter != null) {//推广码正确
                user.setDiamond(user.getDiamond() + 8);
                user.setPromoterId(code);
                userDao.update(user);
                createRecord(user, TranRecord.way.BIND_PROMOTE_CODE.getCode(), TranRecord.itemType.DIAMOND.getCode(), 8);
                result.put("user", user);
                return result;
            } else {
                throw CommonError.PROMOTE_CODE_NOT_EXIST.newException();
            }
        } else {
            throw CommonError.ALREADY_BIND_PROMOTE_CODE.newException();
        }

    }


//	public TextMessage TestConnection() {
//		JsonResult jsonResult=new JsonResult();
//		jsonResult.setError(StatusUtil.SYS_SUCCESS);
//		jsonResult.setPid(PidValue.CHECK_CLIENT);
//		TextMessage textMessage = new TextMessage(jsonResult.toString());
//		return textMessage;
//	}

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


}