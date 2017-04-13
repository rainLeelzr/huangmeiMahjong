package com.huangmei.commonhm.dao;

import com.huangmei.commonhm.model.Score;

public interface ScoreDao extends BaseDao<Integer, Score> {
    Integer selectBestHuType(Integer userId);

    Integer findLastWinnerByRoomId(Integer id);
}