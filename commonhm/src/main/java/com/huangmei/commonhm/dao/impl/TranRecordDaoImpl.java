package com.huangmei.commonhm.dao.impl;

import com.huangmei.commonhm.dao.TranRecordDao;
import com.huangmei.commonhm.model.TranRecord;
import org.springframework.stereotype.Repository;

@Repository
public class TranRecordDaoImpl extends BaseDaoImpl<Integer, TranRecord> implements TranRecordDao {

    @Override
    public Long countForPrizeDraw(TranRecord tranRecord) {
        return sqlSessionTemplate.selectOne(
                statement("countForPrizeDraw"),
                tranRecord
        );
    }
}