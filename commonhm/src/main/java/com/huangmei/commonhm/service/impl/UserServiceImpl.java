package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RecordDao;
import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.dao.TranRecordDao;
import com.huangmei.commonhm.dao.UserDao;
import com.huangmei.commonhm.model.*;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.service.UserService;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.CommonUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.text.DecimalFormat;
import java.util.*;

@Service
public class UserServiceImpl extends BaseServiceImpl<Integer, User> implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoomMemberDao roomMemberDao;
    @Autowired
    private RecordDao recordDao;
    @Autowired
    private TranRecordDao tranRecordDao;

    @Autowired
    private RoomService roomService;

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
        Integer loginType;//1正常登陆,2游戏中断线重连,3结算后未显示结算页面

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
            user.setCoin(0);
            user.setDiamond(0);
            user.setHorn(0);
            Integer uId = CommonUtil.createUserCode();
            List<User> users = userDao.selectAll();
            for (User u : users) {
                if (u.getUId() == uId) {
                    uId = CommonUtil.createUserCode();//确保用户uId的不同
                }
            }

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

            RoomMember roomMember = new RoomMember();
            roomMember.setUserId(user.getId());
            roomMember = roomMemberDao.selectByUserIdForCheck(roomMember);
            if (roomMember != null) {
                loginType = 2;
                Room room = roomService.selectOne(roomMember.getRoomId());
                result.put("room", room);
            } else {
                loginType = 1;
            }
        }
        result.put("user", user);
        result.put("login_type", loginType);
        return result;
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
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> getUser(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(3);
        if (user != null) {
            result.put("user", user);
            Entity.RecordCriteria recordCriteria = new Entity.RecordCriteria();
            recordCriteria.setUserId(Entity.Value.eq(user.getId()));
            long count = recordDao.selectCount(recordCriteria);//总局数
            recordCriteria.setWinType(Entity.Value.ne(0));
            long win_count = recordDao.selectCount(recordCriteria);//胜利局数
            result.put("win", win_count);
            result.put("lose", count - win_count);
            return result;

        } else {
            throw CommonError.USER_NOT_EXIST.newException();
        }

    }

    /**
     * 抽奖
     *
     * @param data
     * @return
     */
    @Override
    public Map<String, Object> prizeDraw(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(3);
        Integer way = TranRecord.way.DRAW_BY_FREE.getCode();
        boolean  judge = data.getBoolean("judge");

            TranRecord tr = new TranRecord();
            tr.setWay(way);
            tr.setUserId(user.getId());
            Long count = tranRecordDao.countForPrizeDraw(tr);

            if (judge){
                if (count<1){
                    result.put("free",true);
                }else {
                    result.put("free",false);
                }
            }else {
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
     * @param user
     * @param result
     * @param way
     * @return
     */
    private Map<String, Object> PrizeRandom(User user, Map<String, Object> result, Integer way) {
        DecimalFormat df = new DecimalFormat("######0.00");
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
        TranRecord tranRecord = new TranRecord();
        tranRecord.setUserId(user.getId());
        tranRecord.setWay(way);
        tranRecord.setTranTimes(new Date());
        String prizeName = prizes.get(random).getPrize_name();
        switch (prizeName) {
            case "100钻石":
                user.setDiamond(user.getDiamond() + 100);
                tranRecord.setQuantity(100);
                tranRecord.setItemType(TranRecord.itemType.DIAMOND.getCode());

                break;
            case "50钻石":
                user.setDiamond(user.getDiamond() + 50);
                tranRecord.setQuantity(50);
                tranRecord.setItemType(TranRecord.itemType.DIAMOND.getCode());
                break;
            case "10钻石":
                user.setDiamond(user.getDiamond() + 10);
                tranRecord.setQuantity(10);
                tranRecord.setItemType(TranRecord.itemType.DIAMOND.getCode());
                break;
            case "5钻石":
                user.setDiamond(user.getDiamond() + 5);
                tranRecord.setQuantity(5);
                tranRecord.setItemType(TranRecord.itemType.DIAMOND.getCode());
                break;
            case "50000金币":
                user.setCoin(user.getCoin() + 50000);
                tranRecord.setQuantity(50000);
                tranRecord.setItemType(TranRecord.itemType.COIN.getCode());
                break;
            case "2钻石":
                user.setDiamond(user.getDiamond() + 2);
                tranRecord.setQuantity(2);
                tranRecord.setItemType(TranRecord.itemType.DIAMOND.getCode());
                break;
            case "2000金币":
                user.setCoin(user.getCoin() + 2000);
                tranRecord.setQuantity(2000);
                tranRecord.setItemType(TranRecord.itemType.COIN.getCode());
                break;
        }
        userDao.update(user);
        tranRecordDao.save(tranRecord);

        result.put("user", user);
        result.put("prize", prizeName);
        return result;
    }

    /**
     * 封装奖品
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
     * 免费领取金币
     * @param data
     * @param user
     * @return
     */
    @Override
    public Map<String, Object> freeCoins(JSONObject data, User user) {
        Map<String, Object> result = new HashMap<String, Object>(3);
        Integer way = TranRecord.way.DRAW_BY_FREE.getCode();

        TranRecord tr = new TranRecord();
        tr.setWay(way);
        tr.setUserId(user.getId());
        Long count = tranRecordDao.countForPrizeDraw(tr);


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

            result.put("way", way);
        }

        return result;


    }

//	public TextMessage TestConnection() {
//		JsonResult jsonResult=new JsonResult();
//		jsonResult.setError(StatusUtil.SYS_SUCCESS);
//		jsonResult.setPid(PidValue.CHECK_CLIENT);
//		TextMessage textMessage = new TextMessage(jsonResult.toString());
//		return textMessage;
//	}


}