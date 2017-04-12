package com.huangmei.commonhm.dao.impl;

import com.huangmei.commonhm.dao.ScoreDao;
import com.huangmei.commonhm.model.Score;
import org.springframework.stereotype.Repository;

@Repository
public class ScoreDaoImpl extends BaseDaoImpl<Integer, Score> implements ScoreDao {

    @Override
    public Integer selectBestHuType(Integer userId) {

        return sqlSessionTemplate.selectOne(
                statement("selectBestHuType"),
                userId
        );
    }
}