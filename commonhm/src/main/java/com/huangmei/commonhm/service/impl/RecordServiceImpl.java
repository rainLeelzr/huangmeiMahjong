package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RecordDao;
import com.huangmei.commonhm.model.Record;
import com.huangmei.commonhm.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl extends BaseServiceImpl<Integer, Record> implements RecordService {
	
	@Autowired
	private RecordDao dao;

}