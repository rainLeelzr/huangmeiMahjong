package com.huangmei.commonhm.dao;

import com.huangmei.commonhm.model.TranRecord;

public interface TranRecordDao extends BaseDao<Integer, TranRecord> {
   Long countForPrizeDraw(TranRecord tranRecord);
}