package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.ScoreDao;
import com.huangmei.commonhm.model.Score;
import com.huangmei.commonhm.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScoreServiceImpl extends BaseServiceImpl<Integer, Score> implements ScoreService {
	
	@Autowired
	private ScoreDao dao;

}