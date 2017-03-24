package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.dao.RoomMemberDao;
import com.huangmei.commonhm.model.RoomMember;
import com.huangmei.commonhm.service.RoomMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoomMemberServiceImpl extends BaseServiceImpl<Integer, RoomMember> implements RoomMemberService {
	
	@Autowired
	private RoomMemberDao dao;

}