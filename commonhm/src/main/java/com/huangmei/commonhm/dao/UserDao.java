package com.huangmei.commonhm.dao;

import com.huangmei.commonhm.model.Score;
import com.huangmei.commonhm.model.User;

public interface UserDao extends BaseDao<Integer, User> {
    void addCoin(Score score);
}