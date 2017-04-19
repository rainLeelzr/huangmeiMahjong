package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RoomDao;
import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.dao.ScoreDao;
import com.huangmei.commonhm.manager.getACard.GetACardManager;
import com.huangmei.commonhm.manager.operate.BaseOperate;
import com.huangmei.commonhm.manager.operate.CanDoOperate;
import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.putOutCard.AfterPutOutCardManager;
import com.huangmei.commonhm.manager.ruanHu.RuanHuManager;
import com.huangmei.commonhm.manager.yingHu.YingHuManager;
import com.huangmei.commonhm.model.*;
import com.huangmei.commonhm.model.mahjong.*;
import com.huangmei.commonhm.model.mahjong.vo.*;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.RoomRedis;
import com.huangmei.commonhm.redis.VersionRedis;
import com.huangmei.commonhm.service.RoomService;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.JsonUtil;
import com.huangmei.commonhm.util.PidValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {

    public static final String PUT_OUT_HANDCARD_KEY = "putOutHandCard";
    private static final Logger log = LoggerFactory.getLogger(GameService.class);
    @Autowired
    private GameRedis gameRedis;

    @Autowired
    private VersionRedis versionRedis;

    @Autowired
    private AfterPutOutCardManager afterPutOutCardManager;

    @Autowired
    private GetACardManager getACardManager;

    @Autowired
    private YingHuManager yingHuManager;

    @Autowired
    private RuanHuManager ruanHuManager;

    @Autowired
    private RoomMemberDao roomMemberDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private ScoreDao scoreDao;

    @Autowired
    private RoomRedis roomRedis;

    @Autowired
    private RoomService roomService;

    /**
     * 判断玩家有没有执行操作的权利
     *
     * @param roomId      玩家所在的房间id
     * @param userId      玩家id
     * @param toDoOperate 玩家需要执行的操作
     */
    private CanDoOperate canOperate(Integer roomId, Integer userId, Operate toDoOperate) {
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(roomId);
        if (!waitingClientOperate.getRoomMember().getUserId().equals(userId)) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }
        if (!waitingClientOperate.getOperates().contains(toDoOperate)) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }
        return waitingClientOperate;
    }

    /**
     * 判断玩家有没有执行操作的权利
     *
     * @param roomId      玩家所在的房间id
     * @param userId      玩家id
     * @param baseOperate 玩家需要执行的操作
     */
    private void canOperate(Integer roomId, Integer userId, BaseOperate baseOperate) {
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(roomId);
        if (!waitingClientOperate.getRoomMember().getUserId().equals(userId)) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }

        boolean match = false;
        for (Operate operate : waitingClientOperate.getOperates()) {
            if (operate.getBaseOperate() == baseOperate) {
                match = true;
            }
        }
        if (!match) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }
    }

    /***
     * 初始化数据包括：骰子的点数、每个人的手牌、剩余的牌等信息。
     * 一局游戏开始时，生成麻将的初始数据。
     */
    public Map<String, Object> putOutHandCard(
            Room room, List<RoomMember> roomMembers)
            throws InstantiationException, IllegalAccessException {

        // 对roomMembers按座位号升序
        Collections.sort(roomMembers, new Comparator<RoomMember>() {
            @Override
            public int compare(RoomMember o1, RoomMember o2) {
                return o1.getSeat() - o2.getSeat();
            }
        });

        Map<String, Object> result = new HashMap<>(6);

        int players = room.getPlayers();
        int bankerSite = 0;
        int nextTimes;//当前是需要开始游戏的第几局

        // 判断是否房间第一局发牌
        MahjongGameData lastMahjongGameData = gameRedis.getMahjongGameData(room.getId());
        if (lastMahjongGameData == null) {
            bankerSite = 1;
            nextTimes = 1;
        } else {
            // 获取上次胡牌的玩家，如果没有胡牌的玩家，则庄家座位为1
            Score score = new Score();
            score.setRoomId(room.getId());
            score.setTimes(lastMahjongGameData.getCurrentTimes());
            Integer lastWinnerUserId = scoreDao.findLastWinnerByRoomId(score);
            //Integer lastWinnerUserId = lastMahjongGameData.getPersonalCardInfos().get(0).getRoomMember().getUserId();
            if (lastWinnerUserId != null) {
                for (PersonalCardInfo personalCardInfo : lastMahjongGameData.getPersonalCardInfos()) {
                    RoomMember roomMember = personalCardInfo.getRoomMember();
                    if (roomMember.getUserId().equals(lastWinnerUserId)) {
                        bankerSite = roomMember.getSeat();
                    }
                }
                if (bankerSite == 0) {
                    throw CommonError.REDIS_GAME_DATA_ERROR.newException();
                }
            } else {
                bankerSite = 1;
            }

            nextTimes = lastMahjongGameData.getCurrentTimes() + 1;
        }

        //先设置为userId，在api层转换为uId
        Integer bankerUId = roomMembers.get(bankerSite - 1).getUserId();

        // 初始化一局麻将的数据
        MahjongGameData mahjongGameData = MahjongGameData.initData(players, bankerSite, nextTimes);
        log.debug("初始化一局麻将的数据:{}", JsonUtil.toJson(mahjongGameData));

        mahjongGameData.setRoomType(room.getType());
        mahjongGameData.setTimes(room.getTimes());

        // 获取新版本号
        Long version = versionRedis.nextVersion(room.getId());
        mahjongGameData.setVersion(version);

        // roomMember改为游戏中
        for (RoomMember roomMember : roomMembers) {
            roomMember.setState(RoomMember.state.PLAYING.getCode());
            roomMemberDao.update(roomMember);
            roomRedis.editRoom(roomMember);
            roomRedis.joinRoom(roomMember);
        }

        // 游戏开始数据
        GameStartVo gameStartVo = new GameStartVo();

        // 添加roomMember，拆分成4份手牌数据，传给客户端
        List<FirstPutOutCard> firstPutOutCards = new ArrayList<>(players);
        for (int i = 0; i < players; i++) {
            PersonalCardInfo personalCardInfo = mahjongGameData.getPersonalCardInfos().get(i);
            // 添加玩家信息RoomMember
            personalCardInfo.setRoomMember(roomMembers.get(i));

            // 设置游戏开始数据
            if (i == 0) {
                gameStartVo.setBankerUId(bankerUId);//先设置为userId，在api层转换为uId
                gameStartVo.setDices(mahjongGameData.getDices());
                gameStartVo.setBaoMotherId(mahjongGameData.getBaoMother().getId());
                gameStartVo.setCurrentTimes(mahjongGameData.getCurrentTimes());

                List<Integer> baoMahjongIds = new ArrayList<>(mahjongGameData.getBaoMahjongs().size());
                for (Mahjong mahjong : mahjongGameData.getBaoMahjongs()) {
                    baoMahjongIds.add(mahjong.getId());
                }
                gameStartVo.setBaoMahjongs(baoMahjongIds);
            }

            FirstPutOutCard fpc = new FirstPutOutCard();
            fpc.setuId(personalCardInfo.getRoomMember().getUserId());//先设置为userId，在api层转换为uId
            fpc.setHandCardIds(Mahjong.parseToIds(personalCardInfo.getHandCards()));
            fpc.setLeftCardCount(mahjongGameData.getLeftCards().size());

            firstPutOutCards.add(fpc);
        }

        result.put(PUT_OUT_HANDCARD_KEY, firstPutOutCards);
        result.put(GameStartVo.class.getSimpleName(), gameStartVo);

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);
        result.put(MahjongGameData.class.getSimpleName(), mahjongGameData);
        return result;
    }

    /**
     * 打出一张麻将
     */
    public Map<String, Object> playAMahjong(Room room, User user, Mahjong playedMahjong) {
        canOperate(room.getId(), user.getId(), Operate.PLAY_A_MAHJONG);

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 出牌验证
        if (!putOutCardValidate(playedMahjong, mahjongGameData, user)) {
            throw CommonError.USER_NOT_HAVE_SPECIFIED_CARD.newException();
        }

        // 删除redis的等待客户端操作对象waitingClientOperate
        gameRedis.deleteWaitingClientOperate(room.getId());

        // 添加打出的麻将到游戏数据
        PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(
                mahjongGameData.getPersonalCardInfos(),
                user.getId()
        );
        mahjongGameData.getOutCards().add(new OutCard(playedMahjong, personalCardInfo.getRoomMember()));
        gameRedis.saveMahjongGameData(mahjongGameData);

        // 广播打出的牌
        List<PlayedMahjong> playedMahjongs = playedMahjongBroadcast(mahjongGameData, user, playedMahjong);

        Map<String, Object> result = new HashMap<>(2);
        result.put(PlayedMahjong.class.getSimpleName(), playedMahjongs);
        result.put(MahjongGameData.class.getSimpleName(), mahjongGameData);
        return result;
    }


    /**
     * @param mahjongGameData mahjongGameData
     * @param user            打出牌的用户
     * @param playedMahjong   打出的麻将
     * @return
     */
    private List<PlayedMahjong> playedMahjongBroadcast(MahjongGameData mahjongGameData, User user, Mahjong
            playedMahjong) {
        List<PlayedMahjong> playedMahjongs = new ArrayList<>(mahjongGameData.getPersonalCardInfos().size());
        PersonalCardInfo playedPersonalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);
        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            PlayedMahjong temp = new PlayedMahjong();
            temp.setuId(personalCardInfo.getRoomMember().getUserId());//先设置为userid，在api转uid
            temp.setLeftCardCount(mahjongGameData.getLeftCards().size());
            temp.setPlayedMahjongId(playedMahjong.getId());
            temp.setPlayedUId(user.getUId());
            temp.setHandCardIds(Mahjong.parseToIds(playedPersonalCardInfo.getHandCards()));
            temp.setPengMahjongIds(Mahjong.parseCombosToMahjongIds(playedPersonalCardInfo.getPengs()));
            temp.setGangs(GangVo.parseFromGangCombos(playedPersonalCardInfo.getGangs()));
            temp.setVersion(mahjongGameData.getVersion());
            playedMahjongs.add(temp);
        }
        return playedMahjongs;
    }

    /**
     * 玩家操作时，验证其版本号
     */
    //private void validateVersion(Room room, long version) {
    //    Long nowVersion = versionRedis.nowVersion(room.getId());
    //    if (!nowVersion.equals(version)) {
    //        throw CommonError.SYS_VERSION_TIMEOUT.newException();
    //    }
    //}


    /**
     * 客户端打出一张牌的处理逻辑。
     * 遍历其他3个玩家，判断是否有碰，明杠，吃胡。
     * 如果有，则按优先级降序，存进redis的
     * 选择优先级最高的操作，返回给对应的客户端，等待其选择执行操作或着选择过
     * 如果选择过，则读redis待操作集合，循环上一步操作。
     * 如果全部人都选择过（待操作集合为空），则进入下家摸牌方法。
     * 如果待操作集合中有人选择了执行操作，则清空待操作集合，执行相应操作。
     *
     * @param putOutMahjong 打出的牌对象
     * @param room          用户所在的房间
     * @param version       消息版本号
     */

    public void putOutCard(Mahjong putOutMahjong, Room room, User user,
                           Long version)
            throws InstantiationException, IllegalAccessException {
        // 验证版本号
        //validateVersion(room, version);

        // 取出麻将数据
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(
                room.getId());

        // 出牌验证
        if (!putOutCardValidate(putOutMahjong, mahjongGameData, user)) {
            throw CommonError.USER_NOT_HAVE_SPECIFIED_CARD.newException();
        }

        for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
            log.debug("验证后座位{}的手牌：{}{}",
                    personalCardInfo.getRoomMember().getSeat(),
                    personalCardInfo.getHandCards().size(),
                    personalCardInfo.getHandCards());
        }

        // 扫描其他用户是否有吃胡、大明杠、碰的操作
        List<CanDoOperate> canOperates =
                afterPutOutCardManager.scan(mahjongGameData, putOutMahjong, user);
        log.debug("扫描出来可以的操作：{}", canOperates);


    }

    /**
     * 验证客户端出的牌是否合法，合法则在玩家的PersonalCardInfo手牌集合中移除该麻将牌
     */
    private boolean putOutCardValidate(Mahjong putOutCard, MahjongGameData
            mahjongGameData, User user) {
        // 获取玩家手牌信息
        PersonalCardInfo personalCardInfo =
                PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user.getId());

        // 判断客户端打出的牌是不是刚摸上手的
        if (putOutCard == personalCardInfo.getTouchMahjong()) {
            personalCardInfo.setTouchMahjong(null);
            return true;
        }

        // 判断客户端打出的牌是不是其拥有的手牌
        boolean isHandCard = false;
        Iterator<Mahjong> iterator = personalCardInfo.getHandCards().iterator();
        while (iterator.hasNext()) {
            Mahjong mahjong = iterator.next();
            if (mahjong == putOutCard) {
                iterator.remove();
                isHandCard = true;
                break;
            }
        }
        // 在碰的情况下，personalCardInfo.getTouchMahjong()是null
        if (isHandCard && personalCardInfo.getTouchMahjong() != null) {
            // 把摸到的麻将放到手牌列表
            personalCardInfo.getHandCards().add(personalCardInfo.getTouchMahjong());
        }
        return isHandCard;

    }

    /**
     * 处理硬暗杠的请求逻辑
     */
    public MahjongGameData yingAnGang(User user, Room room, List<Mahjong> toBeGangMahjongs) {
        // 判断需要暗杠的牌是否一样
        if (!Mahjong.isSame(toBeGangMahjongs)) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(room.getId());
        if (!waitingClientOperate.getRoomMember().getUserId().equals(user.getId())) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 玩家个人卡信息
        PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);

        // 判断玩家是否含有暗杠的牌
        boolean isYingAnGang = PersonalCardInfo.hasMahjongsWithTouchMahjong(personalCardInfo, toBeGangMahjongs);
        if (!isYingAnGang) {
            throw CommonError.USER_NOT_HAVE_SPECIFIED_CARD.newException();
        }

        // 玩家的个人卡信息中添加杠列表
        Combo gang = Combo.newYingAnGang(toBeGangMahjongs);
        personalCardInfo.getGangs().add(gang);

        // 玩家的个人卡信息的手牌中移除已杠的麻将
        personalCardInfo.getHandCards().add(personalCardInfo.getTouchMahjong());
        personalCardInfo.setTouchMahjong(null);
        personalCardInfo.getHandCards().removeAll(toBeGangMahjongs);

        gameRedis.saveMahjongGameData(mahjongGameData);
        return mahjongGameData;
    }

    /**
     * 处理软暗杠的请求逻辑
     */
    public MahjongGameData ruanAnGang(User user, Room room, List<Mahjong> toBeGangMahjongs) {
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(room.getId());
        if (!waitingClientOperate.getRoomMember().getUserId().equals(user.getId())) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }

        // 判断需要暗杠的牌是否一样
        if (Mahjong.isSame(toBeGangMahjongs)) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 玩家个人卡信息
        PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);

        // 判断玩家是否含有软暗杠的牌
        boolean isRuanAnGang = isRuanAnGang(personalCardInfo, toBeGangMahjongs, mahjongGameData.getBaoMahjongs());
        if (!isRuanAnGang) {
            throw CommonError.USER_NOT_HAVE_SPECIFIED_CARD.newException();
        }

        // 玩家的个人卡信息中添加杠列表
        Combo gang = Combo.newRuanAnGang(toBeGangMahjongs);
        personalCardInfo.getGangs().add(gang);

        // 玩家的个人卡信息的手牌中移除已杠的麻将
        personalCardInfo.getHandCards().add(personalCardInfo.getTouchMahjong());
        personalCardInfo.setTouchMahjong(null);
        personalCardInfo.getHandCards().removeAll(toBeGangMahjongs);

        gameRedis.saveMahjongGameData(mahjongGameData);
        return mahjongGameData;
    }

    /**
     * 验证玩家提交的软暗杠请求是否正确
     */
    private boolean isRuanAnGang(PersonalCardInfo personalCardInfo, List<Mahjong> toBeGangMahjongs, List<Mahjong> baoMahjongs) {
        // 判断玩家的牌中是否含有提交过来杠的牌
        boolean hasToBeGangMahjong = PersonalCardInfo.hasMahjongsWithTouchMahjong(personalCardInfo, toBeGangMahjongs);
        if (!hasToBeGangMahjong) {
            throw CommonError.USER_NOT_HAVE_SPECIFIED_CARD.newException();
        }

        // 判断toBeGangMahjongs有没有宝牌
        Integer baoMahjongNumber = baoMahjongs.get(0).getNumber();
        List<Integer> baoMahjongIndex = new ArrayList<>(toBeGangMahjongs.size() - 1);
        for (int i = 0; i < toBeGangMahjongs.size(); i++) {
            Mahjong toBeGangMahjong = toBeGangMahjongs.get(i);
            if (toBeGangMahjong.getNumber().equals(baoMahjongNumber)) {
                baoMahjongIndex.add(i);
            }
        }
        if (baoMahjongIndex.size() == 0) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        // 判断需要杠的牌中，除了宝牌以外的牌是否一样
        Integer gangNumber = null;
        for (int i = 0; i < toBeGangMahjongs.size(); i++) {
            if (baoMahjongIndex.contains(i)) {
                continue;
            }
            if (gangNumber == null) {
                gangNumber = toBeGangMahjongs.get(i).getNumber();
            } else {
                if (!gangNumber.equals(toBeGangMahjongs.get(i).getNumber())) {
                    throw CommonError.SYS_PARAM_ERROR.newException();
                }
            }
        }

        return true;

    }

    /**
     * 验证玩家提交的硬加杠请求是否正确
     */
    public Object[] yingJiaGang(User user, Room room, Mahjong mahjong) {
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(room.getId());
        if (!waitingClientOperate.getRoomMember().getUserId().equals(user.getId())) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 玩家个人卡信息
        PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);

        // 判断玩家是否含有碰
        List<Combo> pengs = personalCardInfo.getPengs();
        if (pengs.size() == 0) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        // 找到需要加杠的碰
        Combo toBeJiaBangCombo = null;
        for (Combo peng : pengs) {
            if (peng.getYingRuan() == YingRuan.YING) {
                List<Mahjong> mahjongs = peng.getMahjongs();
                if (mahjong.getNumber().equals(mahjongs.get(0).getNumber())) {
                    toBeJiaBangCombo = peng;
                    break;
                }
            }
        }
        if (toBeJiaBangCombo == null) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        // 添加杠combo
        List<Mahjong> yingjiaGangMahjongs = new ArrayList<>(toBeJiaBangCombo.getMahjongs());
        yingjiaGangMahjongs.add(mahjong);
        Combo jiaGangCombo = Combo.newYingJiaGang(yingjiaGangMahjongs);
        personalCardInfo.getGangs().add(jiaGangCombo);

        // 删除碰combo
        pengs.remove(toBeJiaBangCombo);

        // 玩家的个人卡信息的手牌中移除已杠的麻将
        personalCardInfo.getHandCards().add(personalCardInfo.getTouchMahjong());
        personalCardInfo.setTouchMahjong(null);
        personalCardInfo.getHandCards().remove(mahjong);

        gameRedis.saveMahjongGameData(mahjongGameData);
        return new Object[]{mahjongGameData, jiaGangCombo};
    }

    /**
     * 验证玩家提交的软加杠请求是否正确
     */
    public Object[] ruanJiaGang(User user, Room room, List<Mahjong> mahjongs) {
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(room.getId());
        if (!waitingClientOperate.getRoomMember().getUserId().equals(user.getId())) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 玩家个人卡信息
        PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);

        // 判断玩家是否含有碰
        List<Combo> pengs = personalCardInfo.getPengs();
        if (pengs.size() == 0) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        // 分析mahjongs是不是软加杠组合，并找到需要被杠的麻将
        if (Mahjong.isSame(mahjongs)) {// 如果麻将都一样，则不是软加杠
            throw CommonError.SYS_PARAM_ERROR.newException();
        }
        Integer baoMahjongNumber = mahjongGameData.getBaoMahjongs().get(0).getNumber();
        Mahjong beJiaGangMahjong = null;
        for (Mahjong mahjong : mahjongs) {
            if (!baoMahjongNumber.equals(mahjong.getNumber())) {
                if (beJiaGangMahjong == null) {
                    beJiaGangMahjong = mahjong;
                } else {
                    if (!beJiaGangMahjong.getNumber().equals(mahjong.getNumber())) {
                        // 出现了与宝牌和beJiaGangMahjong都不相同的麻将，即mahjongs出现了3种麻将，则加杠麻将参数错误
                        throw CommonError.SYS_PARAM_ERROR.newException();
                    }
                }

            }
        }

        // 找到需要加杠的碰组合。
        // 如果是硬碰，则需要有宝牌才能加杠
        // 如果是软碰，则有宝牌和碰牌都可以加杠
        Combo toBeJiaBangCombo = null;
        for (Combo peng : pengs) {
            for (Mahjong tempMahjong : peng.getMahjongs()) {
                // 如果这个combo组合是宝牌归位碰，或者不等于beJiaGangMahjong,则不是需要加杠的组合
                if (tempMahjong.getNumber().equals(baoMahjongNumber)) {
                    if (peng.getYingRuan() == YingRuan.YING) {
                        break;
                    }
                } else if (!tempMahjong.getNumber().equals(beJiaGangMahjong.getNumber())) {
                    break;
                }
            }
            toBeJiaBangCombo = peng;
            break;
        }
        if (toBeJiaBangCombo == null) {
            throw CommonError.USER_NOT_HAVE_SPECIFIED_CARD.newException();
        }

        // 添加杠combo
        Combo jiaGangCombo = Combo.newRuanJiaGang(mahjongs);
        personalCardInfo.getGangs().add(jiaGangCombo);

        // 删除碰combo
        pengs.remove(toBeJiaBangCombo);

        // 玩家的个人卡信息的手牌中移除已杠的麻将
        personalCardInfo.getHandCards().add(personalCardInfo.getTouchMahjong());
        personalCardInfo.setTouchMahjong(null);
        personalCardInfo.getHandCards().remove(mahjongs.get(mahjongs.size() - 1));

        gameRedis.saveMahjongGameData(mahjongGameData);
        return new Object[]{mahjongGameData, jiaGangCombo};
    }

    /**
     * 执行硬大明杠的逻辑
     */
    public Object[] yingDaMingGang(User user, Room room, List<Mahjong> mahjongs) {
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(room.getId());
        if (!waitingClientOperate.getRoomMember().getUserId().equals(user.getId())) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 玩家个人卡信息
        PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);

        // 判断玩家手牌是否有3只跟别的玩家打出一样的麻将
        Mahjong playedMahjong = mahjongs.remove(mahjongs.size() - 1);
        if (!personalCardInfo.getHandCards().containsAll(mahjongs)) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        mahjongs.add(playedMahjong);

        // 添加杠combo
        Combo yingDaMingGangCombo = Combo.newYingDaMingGang(mahjongs);
        personalCardInfo.getGangs().add(yingDaMingGangCombo);

        // 玩家的个人卡信息的手牌中移除已杠的麻将
        personalCardInfo.getHandCards().removeAll(mahjongs);

        gameRedis.saveMahjongGameData(mahjongGameData);
        return new Object[]{mahjongGameData, yingDaMingGangCombo};
    }

    /**
     * 执行碰的逻辑
     */
    public Object[] peng(User user, Room room, List<Mahjong> mahjongs) {
        CanDoOperate canDoOperate = canOperate(room.getId(), user.getId(), Operate.YING_PENG);

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 玩家个人卡信息
        PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);

        // 判断玩家手牌是否有2只跟别的玩家打出一样的麻将
        Mahjong playedMahjong = mahjongs.remove(mahjongs.size() - 1);
        if (!personalCardInfo.getHandCards().containsAll(mahjongs)) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        mahjongs.add(playedMahjong);

        // 添加碰combo
        Combo yingDaMingGangCombo = Combo.newPeng(mahjongs);
        personalCardInfo.getPengs().add(yingDaMingGangCombo);

        // 玩家的个人卡信息的手牌中移除已碰的麻将
        personalCardInfo.getHandCards().removeAll(mahjongs);

        gameRedis.saveMahjongGameData(mahjongGameData);

        // 添加可以打牌操作
        Set<Operate> newOperates = new HashSet<>(2);
        newOperates.add(Operate.PLAY_A_MAHJONG);
        canDoOperate.setOperates(newOperates);
        // 保存可操作列表到redis，记录正在等待哪个玩家的什么操作
        gameRedis.saveWaitingClientOperate(canDoOperate);

        return new Object[]{mahjongGameData, yingDaMingGangCombo};
    }

    /**
     * 在软碰或软杠的组合中，找到真正碰的牌，即找到非宝牌的牌
     * 例如碰：一万 一万 五筒
     * 五筒是宝牌，则会返回真正碰的牌：一万
     *
     * @param mahjongs         碰或杠的麻将列表，最多只能出现两种麻将，
     * @param baoMahjongNumber 宝牌的number
     * @return 真正碰的牌
     */
    private Mahjong findReallyMahjong(List<Mahjong> mahjongs, Integer baoMahjongNumber) {
        Mahjong reallyMahjong = null;
        Mahjong baoMahjong = null;
        for (Mahjong mahjong : mahjongs) {
            if (!baoMahjongNumber.equals(mahjong.getNumber())) {
                if (reallyMahjong == null) {
                    reallyMahjong = mahjong;
                } else {
                    if (!reallyMahjong.getNumber().equals(mahjong.getNumber())) {
                        throw CommonError.SYS_PARAM_ERROR.newException();
                    }
                }
            } else {
                baoMahjong = mahjong;
            }
        }
        if (baoMahjong == null) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }
        if (reallyMahjong == null) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }
        return reallyMahjong;
    }

    /**
     * 执行软大明杠的逻辑
     */
    public Object[] ruanDaMingGang(User user, Room room, List<Mahjong> mahjongs) {
        canOperate(room.getId(), user.getId(), Operate.RUAN_DA_MING_GANG);


        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        // 验证需要软大明杠的麻将是否符合规则
        findReallyMahjong(mahjongs, mahjongGameData.getBaoMahjongs().get(0).getNumber());

        // 玩家个人卡信息
        PersonalCardInfo personalCardInfo =
                PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);

        // 判断玩家手牌是否有3只跟别的玩家打出一样的麻将
        Mahjong playedMahjong = mahjongs.remove(mahjongs.size() - 1);
        if (!personalCardInfo.getHandCards().containsAll(mahjongs)) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        mahjongs.add(playedMahjong);

        // 添加杠combo
        Combo ruanDaMingGangCombo = Combo.newRuanDaMingGang(mahjongs);
        personalCardInfo.getGangs().add(ruanDaMingGangCombo);

        // 玩家的个人卡信息的手牌中移除已杠的麻将
        personalCardInfo.getHandCards().removeAll(mahjongs);

        gameRedis.saveMahjongGameData(mahjongGameData);
        return new Object[]{mahjongGameData, ruanDaMingGangCombo};
    }


    /**
     * 从redis获取clientOperateQueue，下一个可以操作的人
     */
    public Object[] guo(User user, Room room) {
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(room.getId());
        if (!waitingClientOperate.getRoomMember().getUserId().equals(user.getId())) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }
        if (!waitingClientOperate.getOperates().contains(Operate.GUO)) {
            throw CommonError.NOT_YOUR_TURN.newException();
        }

        CanDoOperate nextCanDoOperate = gameRedis.getNextCanDoOperate(room.getId());

        if (nextCanDoOperate != null) {
            gameRedis.saveWaitingClientOperate(nextCanDoOperate);
        }

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        return new Object[]{nextCanDoOperate, waitingClientOperate, mahjongGameData};

    }

    /**
     * 硬自摸处理逻辑
     */
    public Object[] yingZiMo(Room room, User user) throws InstantiationException, IllegalAccessException {
        canOperate(room.getId(), user.getId(), BaseOperate.HU);

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        Mahjong specialMahjong = mahjongGameData.getTouchMahjongs().get(mahjongGameData.getTouchMahjongs().size() - 1).getMahjong();

        // 获取胡牌类型
        List<CanDoOperate> canOperates = yingHuManager.scan(
                mahjongGameData,
                specialMahjong,
                user
        );

        if (canOperates.isEmpty()) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        List<Score> scores = genScores4Game(mahjongGameData, room, user, canOperates.get(0));

        for (Score score : scores) {
            scoreDao.save(score);
        }

        // 为下一局游戏做准备，或者结束游戏
        List<SingleUserGameScoreVo> singleUserGameScoreVos = ready4NextGameOrFinishGame(mahjongGameData, room, scores);

        return new Object[]{scores, mahjongGameData, specialMahjong, singleUserGameScoreVos};
    }

    /**
     * 为下一局游戏做准备，或者结束游戏
     * 金币房所有玩家变成待准备状态，房间状态改为待开始
     * 好友房在没有达到局数上限时，所有玩家变成待准备状态，房间状态改为待开始
     */
    private List<SingleUserGameScoreVo> ready4NextGameOrFinishGame(MahjongGameData mahjongGameData, Room room, List<Score> scores) {
        List<SingleUserGameScoreVo> singleUserGameScoreVos = null;

        if (mahjongGameData.getRoomType().equals(Room.type.COINS_ROOM)
                || mahjongGameData.getCurrentTimes() < mahjongGameData.getTimes()) {
            // 还没到最后一局，可以继续一下局
            room.setState(Room.state.wait.getCode());
            roomDao.update(room);

            for (PersonalCardInfo personalCardInfo : mahjongGameData.getPersonalCardInfos()) {
                // roomMember改待准备
                personalCardInfo.getRoomMember().setState(Room.state.wait.getCode());
                roomRedis.editRoom(personalCardInfo.getRoomMember());
                roomRedis.joinRoom(personalCardInfo.getRoomMember());

                RoomMember temp = new RoomMember();
                temp.setId(personalCardInfo.getRoomMember().getId());
                temp.setState(Room.state.wait.getCode());
                roomMemberDao.update(temp);
            }
        } else {
            // 最后一局已结束，计算总结算
            roomService.outRoom(room);
            singleUserGameScoreVos = calculateTotalScore(mahjongGameData);
        }
        return singleUserGameScoreVos;
    }

    /**
     * 计算房间总结算
     */
    private List<SingleUserGameScoreVo> calculateTotalScore(MahjongGameData mahjongGameData) {
        List<SingleUserGameScoreVo> singleUserGameScoreVos = new ArrayList<>(mahjongGameData.getPersonalCardInfos().size());
        for (PersonalCardInfo cardInfo : mahjongGameData.getPersonalCardInfos()) {
            SingleUserGameScoreVo userGameScoreVo = new SingleUserGameScoreVo();
            userGameScoreVo.setuId(cardInfo.getRoomMember().getUserId());// 先设置为userId，在api层转为uId
            singleUserGameScoreVos.add(userGameScoreVo);
        }

        Integer roomId = mahjongGameData.getRoomId();

        // 总结算
        // 自摸次数
        Score scoreQueryParam = new Score();
        scoreQueryParam.setRoomId(roomId);
        scoreQueryParam.setIsZiMo(Score.IsZiMo.ZI_MO.getId());
        List<Score> ziMoTimes = scoreDao.ziMoTimes(scoreQueryParam);
        for (Score ziMoTime : ziMoTimes) {
            for (SingleUserGameScoreVo singleUserGameScoreVo : singleUserGameScoreVos) {
                if (singleUserGameScoreVo.getuId().equals(ziMoTime.getUserId())) {
                    singleUserGameScoreVo.setZiMo(ziMoTime.getIsZiMo());
                    break;
                }
            }
        }

        // 杠次数、总分数
        List<Score> scoreAndGangTimes = scoreDao.scoreAndGangTimes(roomId);
        for (Score scoreAndGangTime : scoreAndGangTimes) {
            for (SingleUserGameScoreVo singleUserGameScoreVo : singleUserGameScoreVos) {
                if (singleUserGameScoreVo.getuId().equals(scoreAndGangTime.getUserId())) {
                    singleUserGameScoreVo.setAnGang(scoreAndGangTime.getAnGangTimes());
                    singleUserGameScoreVo.setAnGang(scoreAndGangTime.getMingGangTimes());
                    singleUserGameScoreVo.setScore(scoreAndGangTime.getScore());
                    break;
                }
            }
        }

        // 接炮次数
        List<Score> jiePaoTimes = scoreDao.jiePaoTimes(roomId);
        for (Score jiePaoTime : jiePaoTimes) {
            for (SingleUserGameScoreVo singleUserGameScoreVo : singleUserGameScoreVos) {
                if (singleUserGameScoreVo.getuId().equals(jiePaoTime.getUserId())) {
                    singleUserGameScoreVo.setAnGang(jiePaoTime.getJiePaoUserId());
                    break;
                }
            }
        }

        // 点炮次数
        List<Score> dianPaoTimes = scoreDao.dianPaoTimes(roomId);
        for (Score dianPaoTime : dianPaoTimes) {
            for (SingleUserGameScoreVo singleUserGameScoreVo : singleUserGameScoreVos) {
                if (singleUserGameScoreVo.getuId().equals(dianPaoTime.getUserId())) {
                    singleUserGameScoreVo.setAnGang(dianPaoTime.getDianPaoUserId());
                    break;
                }
            }
        }

        return singleUserGameScoreVos;
    }

    /**
     * 软自摸处理逻辑
     */
    public Object[] ruanZiMo(Room room, User user) throws InstantiationException, IllegalAccessException {
        canOperate(room.getId(), user.getId(), BaseOperate.HU);

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        Mahjong specialMahjong = mahjongGameData.getTouchMahjongs().get(mahjongGameData.getTouchMahjongs().size() - 1).getMahjong();

        // 获取胡牌类型
        List<CanDoOperate> canOperates = ruanHuManager.scan(
                mahjongGameData,
                specialMahjong,
                user
        );

        if (canOperates.isEmpty()) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        Date now = new Date();

        List<Score> scores = new ArrayList<>(mahjongGameData.getPersonalCardInfos().size());

        for (int i = 0; i < mahjongGameData.getPersonalCardInfos().size(); i++) {
            PersonalCardInfo personalCardInfo = mahjongGameData.getPersonalCardInfos().get(i);
            Score score = new Score();
            score.setRoomId(room.getId());
            score.setUserId(personalCardInfo.getRoomMember().getUserId());
            score.setCreatedTime(now);
            score.setType(room.getType());
            score.setTimes(mahjongGameData.getCurrentTimes());


            boolean isWinner = personalCardInfo.getRoomMember().getUserId().equals(user.getId());

            // 杠数量
            int anGangTimes = 0;
            int mingGangTimes = 0;
            List<Combo> gangCombos = personalCardInfo.getGangs();
            for (Combo gangCombo : gangCombos) {
                if (gangCombo.getPidValue() == PidValue.YING_AN_GANG) {
                    anGangTimes++;
                } else if (gangCombo.getPidValue() == PidValue.YING_DA_MING_GANG
                        || gangCombo.getPidValue() == PidValue.YING_JIA_GANG) {
                    mingGangTimes++;
                }
            }
            score.setAnGangTimes(anGangTimes);
            score.setMingGangTimes(mingGangTimes);

            if (isWinner) {
                score.setIsZiMo(Score.IsZiMo.ZI_MO.getId());
                score.setWinType(Score.WinType.ZI_MO.getId());

                // 设置胡牌类型
                Operate operate = null;
                for (Operate temp : canOperates.get(0).getOperates()) {
                    operate = temp;
                    break;
                }

                Score.HuType huType = Score.HuType.parse(operate);
                score.setHuType(huType.getId());

                // 计算总炮数
                calculatePaoNum(
                        mahjongGameData,
                        personalCardInfo,
                        huType,
                        false,
                        false,
                        room.getMultiple(),
                        score);
            } else {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setScore(0);
                score.setPaoNum(0);
                score.setCoin(0);
                score.setWinType(Score.WinType.OTHER_USER_ZI_MO.getId());
            }

            scores.add(score);
        }

        for (Score score : scores) {
            scoreDao.save(score);
        }

        // 为下一局游戏做准备，或者结束游戏
        List<SingleUserGameScoreVo> singleUserGameScoreVos = ready4NextGameOrFinishGame(mahjongGameData, room, scores);

        return new Object[]{scores, mahjongGameData, specialMahjong, singleUserGameScoreVos};
    }

    /**
     * 软吃胡处理逻辑
     */
    public Object[] ruanChiHu(Room room, User user) throws InstantiationException, IllegalAccessException {
        canOperate(room.getId(), user.getId(), BaseOperate.HU);

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        OutCard outCard = mahjongGameData.getOutCards().get(mahjongGameData.getOutCards().size() - 1);
        Mahjong specialMahjong = outCard.getMahjong();

        // 获取胡牌类型
        List<CanDoOperate> canOperates = ruanHuManager.scan(
                mahjongGameData,
                specialMahjong,
                user
        );

        if (canOperates.isEmpty()) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        Date now = new Date();

        List<Score> scores = new ArrayList<>(mahjongGameData.getPersonalCardInfos().size());

        for (int i = 0; i < mahjongGameData.getPersonalCardInfos().size(); i++) {
            PersonalCardInfo personalCardInfo = mahjongGameData.getPersonalCardInfos().get(i);
            Score score = new Score();
            score.setRoomId(room.getId());
            score.setUserId(personalCardInfo.getRoomMember().getUserId());
            score.setCreatedTime(now);
            score.setType(room.getType());
            score.setTimes(mahjongGameData.getCurrentTimes());


            boolean isWinner = personalCardInfo.getRoomMember().getUserId().equals(user.getId());

            // 杠数量
            int anGangTimes = 0;
            int mingGangTimes = 0;
            List<Combo> gangCombos = personalCardInfo.getGangs();
            for (Combo gangCombo : gangCombos) {
                if (gangCombo.getPidValue() == PidValue.YING_AN_GANG) {
                    anGangTimes++;
                } else if (gangCombo.getPidValue() == PidValue.YING_DA_MING_GANG
                        || gangCombo.getPidValue() == PidValue.YING_JIA_GANG) {
                    mingGangTimes++;
                }
            }
            score.setAnGangTimes(anGangTimes);
            score.setMingGangTimes(mingGangTimes);

            score.setJiePaoUserId(user.getId());
            score.setDianPaoUserId(outCard.getRoomMember().getUserId());

            if (isWinner) {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setWinType(Score.WinType.JIE_PAO.getId());

                // 设置胡牌类型
                Operate operate = null;
                for (Operate temp : canOperates.get(0).getOperates()) {
                    operate = temp;
                    break;
                }

                Score.HuType huType = Score.HuType.parse(operate);
                score.setHuType(huType.getId());

                // 计算总炮数
                calculatePaoNum(
                        mahjongGameData,
                        personalCardInfo,
                        huType,
                        false,
                        false,
                        room.getMultiple(),
                        score);
            } else {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setScore(0);
                score.setPaoNum(0);
                score.setCoin(0);
                if (outCard.getRoomMember().getId().equals(personalCardInfo.getRoomMember().getId())) {
                    score.setWinType(Score.WinType.DIAN_PAO.getId());
                } else {
                    score.setWinType(Score.WinType.NONE.getId());
                }
            }

            scores.add(score);
        }

        for (Score score : scores) {
            scoreDao.save(score);
        }

        // 为下一局游戏做准备，或者结束游戏
        List<SingleUserGameScoreVo> singleUserGameScoreVos = ready4NextGameOrFinishGame(mahjongGameData, room, scores);

        return new Object[]{scores, mahjongGameData, specialMahjong, singleUserGameScoreVos};
    }

    /**
     * 硬吃胡处理逻辑
     */
    public Object[] yingChiHu(Room room, User user) throws InstantiationException, IllegalAccessException {
        canOperate(room.getId(), user.getId(), BaseOperate.HU);

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        OutCard outCard = mahjongGameData.getOutCards().get(mahjongGameData.getOutCards().size() - 1);
        Mahjong specialMahjong = outCard.getMahjong();

        // 获取胡牌类型
        List<CanDoOperate> canOperates = yingHuManager.scan(
                mahjongGameData,
                specialMahjong,
                user
        );

        if (canOperates.isEmpty()) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        Date now = new Date();

        List<Score> scores = new ArrayList<>(mahjongGameData.getPersonalCardInfos().size());

        for (int i = 0; i < mahjongGameData.getPersonalCardInfos().size(); i++) {
            PersonalCardInfo personalCardInfo = mahjongGameData.getPersonalCardInfos().get(i);
            Score score = new Score();
            score.setRoomId(room.getId());
            score.setUserId(personalCardInfo.getRoomMember().getUserId());
            score.setCreatedTime(now);
            score.setType(room.getType());
            score.setTimes(mahjongGameData.getCurrentTimes());


            boolean isWinner = personalCardInfo.getRoomMember().getUserId().equals(user.getId());

            // 杠数量
            int anGangTimes = 0;
            int mingGangTimes = 0;
            List<Combo> gangCombos = personalCardInfo.getGangs();
            for (Combo gangCombo : gangCombos) {
                if (gangCombo.getPidValue() == PidValue.YING_AN_GANG) {
                    anGangTimes++;
                } else if (gangCombo.getPidValue() == PidValue.YING_DA_MING_GANG
                        || gangCombo.getPidValue() == PidValue.YING_JIA_GANG) {
                    mingGangTimes++;
                }
            }
            score.setAnGangTimes(anGangTimes);
            score.setMingGangTimes(mingGangTimes);

            score.setJiePaoUserId(user.getId());
            score.setDianPaoUserId(outCard.getRoomMember().getUserId());

            if (isWinner) {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setWinType(Score.WinType.JIE_PAO.getId());

                // 设置胡牌类型
                Operate operate = null;
                for (Operate temp : canOperates.get(0).getOperates()) {
                    operate = temp;
                    break;
                }

                Score.HuType huType = Score.HuType.parse(operate);
                score.setHuType(huType.getId());

                // 计算总炮数
                calculatePaoNum(
                        mahjongGameData,
                        personalCardInfo,
                        huType,
                        false,
                        true,
                        room.getMultiple(),
                        score);
            } else {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setScore(0);
                score.setPaoNum(0);
                score.setCoin(0);
                if (outCard.getRoomMember().getId().equals(personalCardInfo.getRoomMember().getId())) {
                    score.setWinType(Score.WinType.DIAN_PAO.getId());
                } else {
                    score.setWinType(Score.WinType.NONE.getId());
                }
            }

            scores.add(score);
        }

        for (Score score : scores) {
            scoreDao.save(score);
        }

        // 为下一局游戏做准备，或者结束游戏
        List<SingleUserGameScoreVo> singleUserGameScoreVos = ready4NextGameOrFinishGame(mahjongGameData, room, scores);
        return new Object[]{scores, mahjongGameData, specialMahjong, singleUserGameScoreVos};
    }

    /**
     * 硬、软抢杠胡处理逻辑
     */
    public Object[] qiangGangHu(Room room, User user, User gangUser, Mahjong qiangGangMahjong) throws InstantiationException, IllegalAccessException {
        canOperate(room.getId(), user.getId(), BaseOperate.HU);

        // 取出麻将数据对象
        MahjongGameData mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        Mahjong specialMahjong = qiangGangMahjong;

        boolean isYinghu = true;

        // 获取胡牌类型
        List<CanDoOperate> canOperates = yingHuManager.scan(
                mahjongGameData,
                specialMahjong,
                user
        );

        if (canOperates.isEmpty()) {
            isYinghu = false;
            canOperates = ruanHuManager.scan(
                    mahjongGameData,
                    specialMahjong,
                    user
            );
        }

        if (canOperates.isEmpty()) {
            throw CommonError.SYS_PARAM_ERROR.newException();
        }

        Date now = new Date();

        List<Score> scores = new ArrayList<>(mahjongGameData.getPersonalCardInfos().size());

        for (int i = 0; i < mahjongGameData.getPersonalCardInfos().size(); i++) {
            PersonalCardInfo personalCardInfo = mahjongGameData.getPersonalCardInfos().get(i);
            Score score = new Score();
            score.setRoomId(room.getId());
            score.setUserId(personalCardInfo.getRoomMember().getUserId());
            score.setCreatedTime(now);
            score.setType(room.getType());
            score.setTimes(mahjongGameData.getCurrentTimes());


            boolean isWinner = personalCardInfo.getRoomMember().getUserId().equals(user.getId());

            // 杠数量
            int anGangTimes = 0;
            int mingGangTimes = 0;
            List<Combo> gangCombos = personalCardInfo.getGangs();
            for (Combo gangCombo : gangCombos) {
                if (gangCombo.getPidValue() == PidValue.YING_AN_GANG) {
                    anGangTimes++;
                } else if (gangCombo.getPidValue() == PidValue.YING_DA_MING_GANG
                        || gangCombo.getPidValue() == PidValue.YING_JIA_GANG) {
                    mingGangTimes++;
                }
            }
            score.setAnGangTimes(anGangTimes);
            score.setMingGangTimes(mingGangTimes);

            score.setJiePaoUserId(user.getId());
            score.setDianPaoUserId(gangUser.getId());

            if (isWinner) {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setWinType(Score.WinType.JIE_PAO.getId());

                // 设置胡牌类型
                Operate operate = null;
                for (Operate temp : canOperates.get(0).getOperates()) {
                    operate = temp;
                    break;
                }

                Score.HuType huType = Score.HuType.parse(operate);
                score.setHuType(huType.getId());

                // 计算总炮数
                calculatePaoNum(
                        mahjongGameData,
                        personalCardInfo,
                        huType,
                        true,
                        isYinghu,
                        room.getMultiple(),
                        score);
            } else {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setScore(0);
                score.setPaoNum(0);
                score.setCoin(0);
                if (gangUser.getId().equals(personalCardInfo.getRoomMember().getId())) {
                    score.setWinType(Score.WinType.DIAN_PAO.getId());
                } else {
                    score.setWinType(Score.WinType.NONE.getId());
                }
            }

            scores.add(score);
        }

        for (Score score : scores) {
            scoreDao.save(score);
        }

        // 清除被抢杠用户的杠
        PersonalCardInfo gangUserCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), gangUser);
        List<Combo> gangs = gangUserCardInfo.getGangs();
        Iterator<Combo> iterator = gangs.iterator();
        while (iterator.hasNext()) {
            Combo next = iterator.next();
            if (next.getMahjongs().get(0).getNumber().equals(qiangGangMahjong.getNumber())) {
                iterator.remove();
                break;
            }
        }
        gameRedis.saveMahjongGameData(mahjongGameData);

        // 为下一局游戏做准备，或者结束游戏
        List<SingleUserGameScoreVo> singleUserGameScoreVos = ready4NextGameOrFinishGame(mahjongGameData, room, scores);

        return new Object[]{scores, mahjongGameData, specialMahjong, singleUserGameScoreVos};
    }

    /**
     * 暂时只为硬自摸服务，待重构
     *
     * @param user       胡牌的玩家，如果平局，则user为null
     * @param canOperate 用于设置胡牌的玩家的胡牌类型，如果平局，则为null
     */
    private List<Score> genScores4Game(
            MahjongGameData mahjongGameData,
            Room room,
            User user,
            CanDoOperate canOperate) {
        List<Score> scores = new ArrayList<>(mahjongGameData.getPersonalCardInfos().size());

        Operate operate = null;
        if (canOperate != null) {
            for (Operate temp : canOperate.getOperates()) {
                operate = temp;
                break;
            }
        }

        Date now = new Date();

        for (int i = 0; i < mahjongGameData.getPersonalCardInfos().size(); i++) {
            PersonalCardInfo personalCardInfo = mahjongGameData.getPersonalCardInfos().get(i);
            Score score = new Score();
            score.setRoomId(room.getId());
            score.setUserId(personalCardInfo.getRoomMember().getUserId());
            score.setCreatedTime(now);
            score.setType(room.getType());
            score.setTimes(mahjongGameData.getCurrentTimes());

            boolean isWinner = user != null && personalCardInfo.getRoomMember().getUserId().equals(user.getId());

            // 杠数量
            int anGangTimes = 0;
            int mingGangTimes = 0;
            List<Combo> gangCombos = personalCardInfo.getGangs();
            for (Combo gangCombo : gangCombos) {
                if (gangCombo.getPidValue() == PidValue.YING_AN_GANG) {
                    anGangTimes++;
                } else if (gangCombo.getPidValue() == PidValue.YING_DA_MING_GANG
                        || gangCombo.getPidValue() == PidValue.YING_JIA_GANG) {
                    mingGangTimes++;
                }
            }
            score.setAnGangTimes(anGangTimes);
            score.setMingGangTimes(mingGangTimes);

            if (isWinner) {
                score.setIsZiMo(Score.IsZiMo.ZI_MO.getId());
                score.setWinType(Score.WinType.ZI_MO.getId());

                // 设置胡牌类型
                Score.HuType huType = Score.HuType.parse(operate);
                score.setHuType(huType.getId());

                // 计算总炮数
                calculatePaoNum(
                        mahjongGameData,
                        personalCardInfo,
                        huType,
                        false,
                        true,
                        room.getMultiple(),
                        score);
            } else {
                score.setIsZiMo(Score.IsZiMo.NOT_ZI_MO.getId());
                score.setScore(0);
                score.setPaoNum(0);
                score.setCoin(0);
                if (user == null) {
                    // 平局的情况，所有人都是"没有胡牌,不用输分"
                    score.setWinType(Score.WinType.NONE.getId());
                } else {
                    score.setWinType(Score.WinType.OTHER_USER_ZI_MO.getId());
                }

            }

            scores.add(score);
        }

        return scores;
    }

    /**
     * 计算总炮数
     *
     * @param mahjongGameData  mahjongGameData
     * @param personalCardInfo personalCardInfo
     * @param huType           huType
     * @param isQiangGang      是否抢杠
     * @param isYingHu         是否硬胡
     * @param multiple         倍数/底分
     * @param score            分数对象
     */
    private void calculatePaoNum(MahjongGameData mahjongGameData,
                                 PersonalCardInfo personalCardInfo,
                                 Score.HuType huType,
                                 boolean isQiangGang,
                                 boolean isYingHu,
                                 Integer multiple,
                                 Score score) {
        int totalPaoNum = 0;
        // 胡牌形式
        totalPaoNum += huType.getPaoNum();

        //算炮规则一(大胡不参与本类算炮规则)：
        if (!huType.isBigHu()) {
            //自摸
            if (score.getIsZiMo().equals(Score.IsZiMo.ZI_MO.getId())) {
                totalPaoNum += 1;
            }
            //胡牌
            totalPaoNum += 1;
            //门前清1炮
            if (personalCardInfo.getPengs().isEmpty()) {
                List<Combo> gangs = personalCardInfo.getGangs();
                if (gangs.isEmpty()) {
                    totalPaoNum += 1;
                } else {
                    boolean isAllAnGang = true;
                    for (Combo gang : gangs) {
                        if (gang.getPidValue() != PidValue.YING_AN_GANG) {
                            isAllAnGang = false;
                            break;
                        }
                    }
                    if (isAllAnGang) {
                        totalPaoNum += 1;
                    }
                }
            }
        }

        //算炮规则二：
        //碰中，白板1炮
        List<Combo> pengs = personalCardInfo.getPengs();
        for (Combo peng : pengs) {
            if (peng.getMahjongs().get(0).getNumber().equals(Mahjong.HONG_ZHONG_1.getNumber())
                    || peng.getMahjongs().get(0).getNumber().equals(Mahjong.BAI_BAN_1.getNumber())) {
                totalPaoNum += 1;
                break;
            }
        }
        //发财、四个癞子、三个白板未碰、三个红中未碰
        int baoMahjongCount = 0;
        int faiCaiCount = 0;
        int baiBanCount = 0;
        int hongZhongCount = 0;
        for (Mahjong mahjong : personalCardInfo.getHandCards()) {
            if (mahjong.getNumber().equals(Mahjong.FA_CAI_1.getNumber())) {
                faiCaiCount++;
            } else if (mahjong.getNumber().equals(mahjongGameData.getBaoMahjongs().get(0).getNumber())) {
                baoMahjongCount++;
            } else if (mahjong.getNumber().equals(Mahjong.BAI_BAN_1.getNumber())) {
                baiBanCount++;
            } else if (mahjong.getNumber().equals(Mahjong.HONG_ZHONG_1.getNumber())) {
                hongZhongCount++;
            }
        }
        if (baoMahjongCount == 4) {
            totalPaoNum += 10;
        }
        switch (faiCaiCount) {
            case 1:
                totalPaoNum += 1;
                break;
            case 2:
                totalPaoNum += 2;
                break;
            case 3:
                totalPaoNum += 6;
                break;
            case 4:
                totalPaoNum += 10;
                break;
        }
        if (baiBanCount >= 3) {
            totalPaoNum += 2;
        }
        if (hongZhongCount >= 3) {
            totalPaoNum += 2;
        }

        // 暗杠、中，白暗杠、明杠、中，白明杠
        List<Combo> gangs = personalCardInfo.getGangs();
        for (Combo gang : gangs) {
            if (gang.getPidValue() == PidValue.YING_DA_MING_GANG
                    || gang.getPidValue() == PidValue.YING_JIA_GANG) {
                if (gang.getMahjongs().get(0).getNumber().equals(Mahjong.HONG_ZHONG_1.getNumber())
                        || gang.getMahjongs().get(0).getNumber().equals(Mahjong.BAI_BAN_1.getNumber())) {
                    totalPaoNum += 3;
                } else {
                    totalPaoNum += 1;
                }
            } else if (gang.getPidValue() == PidValue.YING_AN_GANG) {
                if (gang.getMahjongs().get(0).getNumber().equals(Mahjong.HONG_ZHONG_1.getNumber())
                        || gang.getMahjongs().get(0).getNumber().equals(Mahjong.BAI_BAN_1.getNumber())) {
                    totalPaoNum += 4;
                } else {
                    totalPaoNum += 2;
                }
            }
        }

        //抢杠
        if (isQiangGang) {
            totalPaoNum += 5;
        }


        //硬胡炮数翻倍
        if (isYingHu) {
            totalPaoNum *= 2;
        }

        // todome 单吊(大胡不参与本类算炮规则)
        if (!huType.isBigHu()) {

        }
        // todome 卡牌(大胡不参与本类算炮规则)
        if (!huType.isBigHu()) {

        }

        //设置最终得分
        score.setPaoNum(totalPaoNum);
        score.setScore(totalPaoNum * multiple);
        score.setCoin(score.getScore());

    }

    /**
     * 增加托管用户
     *
     * @param room 需要被托管的用户所在房间
     * @param user 需要被托管的用户
     */
    public Object[] addTrusteeshipUser(Room room, User user) {
        gameRedis.addTrusteeshipUserId(room.getId(), user.getId());

        MahjongGameData mahjongGameData = null;
        // 取出等待客户端操作对象waitingClientOperate
        CanDoOperate waitingClientOperate = gameRedis.getWaitingClientOperate(room.getId());
        mahjongGameData = gameRedis.getMahjongGameData(room.getId());

        return new Object[]{waitingClientOperate, mahjongGameData};
    }

    /**
     * 移除托管用户
     *
     * @param room 需要被托管的用户所在房间
     * @param user 需要被托管的用户
     */
    public Object[] removeTrusteeshipUser(Room room, User user) {
        gameRedis.removeTrusteeshipUserId(room.getId(), user.getId());
        return null;
    }

    /**
     * 执行流局处理
     */
    public Object[] draw(Room room, MahjongGameData mahjongGameData) {
        List<Score> scores = genScores4Game(mahjongGameData, room, null, null);

        for (Score score : scores) {
            scoreDao.save(score);
        }

        // 为下一局游戏做准备，或者结束游戏
        List<SingleUserGameScoreVo> singleUserGameScoreVos = ready4NextGameOrFinishGame(mahjongGameData, room, scores);

        return new Object[]{scores, singleUserGameScoreVos};
    }

    /**
     * 判断玩家是否在游戏中，是的话，返回roomMember
     * 用户进入了房间，未准备时，踢出房间
     * 用户进入了房间，已准备时，踢出房间
     * 用户进入了房间，已开始游戏，不用踢出房间，如果在金币场，则自动设置用户为托管状态，如果在好友场，则不用自动设置用户为托管状态。
     *
     * @param user 断开连接的玩家
     * @return Object[] [0]:CanDoOperate [1]:MahjongGameData [2]:Room [3]:roomMember
     */
    public Object[] dealDisconnection(User user) {
        Entity.RoomMemberCriteria roomMemberCriteria = new Entity.RoomMemberCriteria();
        roomMemberCriteria.setUserId(Entity.Value.eq(user.getId()));
        roomMemberCriteria.setLeaveTime(Entity.Value.isNull());

        List<RoomMember> roomMembers = roomMemberDao.selectList(roomMemberCriteria);
        if (roomMembers.isEmpty()) {
            return null;
        }

        if (roomMembers.size() > 1) {
            log.warn("webSocket断线时,查询库表[TB_ROOM_MEMBER]，[userId={}]在多个房间中的[leaveTime]不为null,具体roomMember:\n{}",
                    user.getId(), roomMembers);
        }

        RoomMember roomMember = roomMembers.get(0);
        Room room = roomService.selectOne(roomMember.getRoomId());
        if (roomMember.getState().equals(RoomMember.state.PLAYING.getCode())
                && room.getType().equals(Room.type.COINS_ROOM.getCode())) {
            // 金币场,玩家在游戏中，设置为托管状态
            Object[] result = addTrusteeshipUser(room, user);
            return new Object[]{result[0], result[1], room, roomMember};
        } else if (
                roomMember.getState().equals(RoomMember.state.UNREADY.getCode())
                        || roomMember.getState().equals(RoomMember.state.READY.getCode())) {
            // DEBUGING　玩家未点击准备,或已准备，但游戏未开始时，踢出房间
            roomService.outRoom(room.getRoomCode(), user.getId());
        }

        return null;
    }
}