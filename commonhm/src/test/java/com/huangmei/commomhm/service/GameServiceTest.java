package com.huangmei.commomhm.service;

import com.huangmei.AbstractTestClass;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import com.huangmei.commonhm.redis.GameRedis;
import com.huangmei.commonhm.redis.VersionRedis;
import com.huangmei.commonhm.service.impl.GameService;
import com.huangmei.commonhm.util.mock.MockComboMahjongList;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;


public class GameServiceTest extends AbstractTestClass {

    public static final Long mockVersion = 10l;

    private static final Logger log = LoggerFactory.getLogger
            (AbstractTestClass.class);

    @Autowired
    GameService gameService;

    @Autowired
    VersionRedis versionRedis;

    @Autowired
    GameRedis gameRedis;

    /**
     * 硬对对胡{座位4,可以胡、}
     */
    @Test
    public void TestYingPengPengHu() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockYingPengPengHuData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 硬七对 {座位2,可以碰、}, {座位3,可以胡、}
     */
    @Test
    public void TestYingQuiHu() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockYingQiDuiData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }


    /**
     * 硬平胡{座位2,可以碰、}, {座位4,可以胡、}
     */
    @Test
    public void TestYingPingHu() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockYingPingHuData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 软碰碰胡{座位4,可以胡、}
     */
    @Test
    public void TestRuanPengPengHu() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockRuanPengPengHuData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 软七对{座位2,可以碰、}, {座位4,可以胡、}
     */
    @Test
    public void TestRuanQiDui() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockRuanQiDuiData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 软平胡{座位2,可以碰、}, {座位4,可以胡、}
     */
    @Test
    public void TestRuanPingHu() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockRuanPingHuData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 硬杠 {座位2,可以碰、杠、}
     */
    @Test
    public void TestYingDaMingGang() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockYingDaMingGangData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 软大明杠 {座位2,可以碰、杠、}
     */
    @Test
    public void TestRuanDaMingGang() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockRuanDaMingGangData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 软碰 {座位2,可以碰}
     */
    @Test
    public void TestRuanPeng() throws IllegalAccessException, InstantiationException {
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

        // 第一次发牌
        //gameService.firstPutOutCard(room, roomMembers);

        // 第一个人出牌
        //Long version = versionRedis.nextVersion(room.getId());


        MahjongGameData temp = gameRedis.getMahjongGameData(room.getId());
        //temp = gameRedis.getMahjongGameData(room.getId());

        temp = mockRuanPengData(room, roomMembers);
        Long version = versionRedis.nowVersion(room.getId());
        //Long version = temp.getVersion();

        Set<Mahjong> handCards = temp.getPersonalCardInfos().get(0).getHandCards();
        Mahjong putOutCard = null;

        // 随机抽一张出牌
        //int r = RandomUtils.nextInt(handCards.size());
        int i = 0;
        //for (Mahjong handCard : handCards) {
        //    if (r == i) {
        //        putOutCard = handCard;
        //        break;
        //    }
        //    i++;
        //}

        // 随机打出第一张出牌
        for (Mahjong handCard : handCards) {
            putOutCard = handCard;
            break;
        }

        log.debug("随机抽打出第{}张麻将：{}", i + 1, putOutCard);

        User user = new User();
        user.setId(1);
        gameService.putOutCard(putOutCard, room, user, version);
    }

    /**
     * 模拟出GameData
     */
    public MahjongGameData mockYingPingHuData(Room room, RoomMember[] roomMembers) {
        //硬对对胡{座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPengPengHuMahjongs();
        //硬七对  {座位2,可以碰、}, {座位3,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingQiDuiMahjongs();
        //硬平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPingHuMahjongs();
        //软平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPingHuMahjongs();
        //硬杠 {座位2,可以碰、杠、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingDaMingGangMahjongs();
        //软七对
        List<Mahjong> allMahjongs = MockComboMahjongList.getRuanQiDuiMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }

    /**
     * 模拟出软碰碰胡GameData
     */
    public MahjongGameData mockRuanPengPengHuData(Room room, RoomMember[] roomMembers) {
        //软碰碰胡{座位4,可以胡、}
        List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPengPengHuMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }

    /**
     * 模拟出硬对对胡GameData
     */
    public MahjongGameData mockYingPengPengHuData(Room room, RoomMember[] roomMembers) {
        //硬对对胡{座位4,可以胡、}
        List<Mahjong> allMahjongs = MockComboMahjongList.getYingPengPengHuMahjongs();
        //硬七对  {座位2,可以碰、}, {座位3,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingQiDuiMahjongs();
        //硬平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPingHuMahjongs();
        //软平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPingHuMahjongs();
        //硬杠 {座位2,可以碰、杠、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingDaMingGangMahjongs();
        //软七对
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanQiDuiMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }

    /**
     * 模拟出硬七对GameData
     */
    public MahjongGameData mockYingQiDuiData(Room room, RoomMember[] roomMembers) {
        //硬对对胡{座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPengPengHuMahjongs();
        //硬七对  {座位2,可以碰、}, {座位3,可以胡、}
        List<Mahjong> allMahjongs = MockComboMahjongList.getYingQiDuiMahjongs();
        //硬平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPingHuMahjongs();
        //软平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPingHuMahjongs();
        //硬杠 {座位2,可以碰、杠、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingDaMingGangMahjongs();
        //软七对
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanQiDuiMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }


    /**
     * 模拟出软平胡GameData
     */
    public MahjongGameData mockRuanPingHuData(Room room, RoomMember[] roomMembers) {
        //硬对对胡{座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPengPengHuMahjongs();
        //硬七对  {座位2,可以碰、}, {座位3,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingQiDuiMahjongs();
        //硬平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPingHuMahjongs();
        //软平胡 {座位2,可以碰、}, {座位4,可以胡、}
        List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPingHuMahjongs();
        //硬杠 {座位2,可以碰、杠、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingDaMingGangMahjongs();
        //软七对
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanQiDuiMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }


    /**
     * 模拟出硬大明杠GameData
     */
    public MahjongGameData mockYingDaMingGangData(Room room, RoomMember[] roomMembers) {
        //硬对对胡{座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPengPengHuMahjongs();
        //硬七对  {座位2,可以碰、}, {座位3,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingQiDuiMahjongs();
        //硬平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPingHuMahjongs();
        //软平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPingHuMahjongs();
        //硬杠 {座位2,可以碰、杠、}
        List<Mahjong> allMahjongs = MockComboMahjongList.getYingDaMingGangMahjongs();
        //软七对
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanQiDuiMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }

    /**
     * 模拟出软碰GameData
     */
    public MahjongGameData mockRuanPengData(Room room, RoomMember[] roomMembers) {
        List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPengMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }

    /**
     * 模拟出软大明杠GameData
     */
    public MahjongGameData mockRuanDaMingGangData(Room room, RoomMember[] roomMembers) {
        //软杠 {座位2,可以碰、杠、}
        List<Mahjong> allMahjongs = MockComboMahjongList.getRuanDaMingGangMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }

    /**
     * 模拟出软七对GameData
     */
    public MahjongGameData mockRuanQiDuiData(Room room, RoomMember[] roomMembers) {
        //硬对对胡{座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPengPengHuMahjongs();
        //硬七对  {座位2,可以碰、}, {座位3,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingQiDuiMahjongs();
        //硬平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingPingHuMahjongs();
        //软平胡 {座位2,可以碰、}, {座位4,可以胡、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getRuanPingHuMahjongs();
        //硬杠 {座位2,可以碰、杠、}
        //List<Mahjong> allMahjongs = MockComboMahjongList.getYingDaMingGangMahjongs();
        //软七对{座位2,可以碰、}, {座位4,可以胡、}
        List<Mahjong> allMahjongs = MockComboMahjongList.getRuanQiDuiMahjongs();

        log.debug("allMahjongs.size:{}", allMahjongs.size());

        int players = 4;
        int bankerSite = 1;

        // 创建麻将游戏的数据结构
        MahjongGameData mahjongGameData = new MahjongGameData();
        mahjongGameData.setBankerSite(1);
        List<PersonalCardInfo> handCards = new ArrayList<>(players);
        List<Mahjong> leftCards = new ArrayList<>(
                allMahjongs.size() - players * MahjongGameData
                        .HAND_CARD_NUMBER);
        mahjongGameData.setPersonalCardInfos(handCards);
        mahjongGameData.setLeftCards(leftCards);

        // 掷骰
        mahjongGameData.setDices(MahjongGameData.rollDice());

        // 宝牌
        List<Mahjong> baoMahjongs = new ArrayList<>(4);
        baoMahjongs.add(Mahjong.THREE_TIAO_1);
        baoMahjongs.add(Mahjong.THREE_TIAO_2);
        baoMahjongs.add(Mahjong.THREE_TIAO_3);
        baoMahjongs.add(Mahjong.THREE_TIAO_4);
        mahjongGameData.setBaoMahjongs(baoMahjongs);
        log.debug("宝牌:{}", baoMahjongs);

        // 获取新版本号
        Long version = versionRedis.nextVersion(roomMembers[0].getRoomId());
        mahjongGameData.setVersion(version);

        // 打乱所有麻将牌顺序
        //System.out.println("乱序后麻将：" + allMahjongs);

        // allMahjongs的下标，一共120张牌，用于记录分到第几张牌
        int index = 0;

        // 创建每个玩家的手牌对象
        for (int i = 0; i < players; i++) {
            PersonalCardInfo handCard = new PersonalCardInfo();
            Set<Mahjong> mahjongs = new TreeSet<>();
            handCard.setHandCards(mahjongs);
            handCards.add(handCard);
        }

        // 把牌分给玩家
        Set<Mahjong> temp;
        int j = 0;
        for (PersonalCardInfo handCard : handCards) {
            temp = handCard.getHandCards();
            for (int i = 0; i < MahjongGameData.HAND_CARD_NUMBER; i++) {
                temp.add(allMahjongs.get(index++));
            }
            handCard.setRoomMember(roomMembers[j++]);
        }

        // 庄家摸多一张牌
        handCards.get(bankerSite - 1).setTouchMahjong(allMahjongs.get(index++));

        // 剩下的牌放在leftCards
        for (; index < allMahjongs.size(); index++) {
            leftCards.add(allMahjongs.get(index));
        }

        mahjongGameData.setLeftCardCount(leftCards.size());

        // 麻将数据存redis
        gameRedis.saveMahjongGameData(mahjongGameData);

        return mahjongGameData;
    }


}
