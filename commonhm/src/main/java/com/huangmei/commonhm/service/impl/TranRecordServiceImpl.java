package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.TranRecordDao;
import com.huangmei.commonhm.model.TranRecord;
import com.huangmei.commonhm.service.TranRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TranRecordServiceImpl extends BaseServiceImpl<Integer, TranRecord> implements TranRecordService {
	
	@Autowired
	private TranRecordDao dao;

}