package com.huangmei.commonhm.dao.impl;

import com.huangmei.commonhm.dao.ScoreDao;
import com.huangmei.commonhm.model.Score;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ScoreDaoImpl extends BaseDaoImpl<Integer, Score> implements ScoreDao {

    @Override
    public Integer selectBestHuType(Integer userId) {

        return sqlSessionTemplate.selectOne(
                statement("selectBestHuType"),
                userId
        );
    }

    @Override
    public Integer findLastWinnerByRoomId(Score score) {
        Score s = sqlSessionTemplate.selectOne(
                statement("selectLastWinner"),
                score
        );
        if (s == null) {
            if (score.getTimes() > 1) {
                score.setTimes(score.getTimes() - 1);
                return findLastWinnerByRoomId(score);
            } else {
                return null;
            }
        }

        return s.getUserId();
    }

    @Override
    public List<Score> ziMoTimes(Score score) {

        return sqlSessionTemplate.selectList(
                statement("ziMoTimes"),
                score
        );
    }

    @Override
    public List<Score> scoreAndGangTimes(Integer roomId) {
        return sqlSessionTemplate.selectList(
                statement("scoreAndGangTimes"),
                roomId
        );
    }

    @Override
    public List<Score> jiePaoTimes(Integer roomId) {
        return sqlSessionTemplate.selectList(
                statement("jiePaoTimes"),
                roomId
        );
    }

    @Override
    public List<Score> dianPaoTimes(Integer roomId) {
        return sqlSessionTemplate.selectList(
                statement("dianPaoTimes"),
                roomId
        );
    }


}