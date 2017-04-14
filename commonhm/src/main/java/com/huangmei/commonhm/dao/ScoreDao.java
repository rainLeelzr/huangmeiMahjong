package com.huangmei.commonhm.dao;

import com.huangmei.commonhm.model.Score;

import java.util.List;

public interface ScoreDao extends BaseDao<Integer, Score> {
    Integer selectBestHuType(Integer userId);

    Integer findLastWinnerByRoomId(Score score);

    List<Score> ziMoTimes(Score score);

    List<Score> scoreAndGangTimes(Integer roomId);

    List<Score> jiePaoTimes(Integer roomId);

    List<Score> dianPaoTimes(Integer roomId);

}