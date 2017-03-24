package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.NoticeDao;
import com.huangmei.commonhm.model.Notice;
import com.huangmei.commonhm.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends BaseServiceImpl<Integer, Notice> implements NoticeService {
	
	@Autowired
	private NoticeDao dao;

}