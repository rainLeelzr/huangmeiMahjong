package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.VoteDao;
import com.huangmei.commonhm.model.Vote;
import com.huangmei.commonhm.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VoteServiceImpl extends BaseServiceImpl<Integer, Vote> implements VoteService {
	
	@Autowired
	private VoteDao dao;

}