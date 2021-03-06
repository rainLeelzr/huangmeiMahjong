package com.huangmei.commonhm.dao.impl;

import com.huangmei.commonhm.dao.UserDao;
import com.huangmei.commonhm.model.Score;
import com.huangmei.commonhm.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl extends BaseDaoImpl<Integer, User> implements UserDao {

    @Override
    public void addCoin(Score score) {
        sqlSessionTemplate.update("addCoin",
                score);
    }
}