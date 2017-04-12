package com.huangmei.commonhm.service.impl;

import com.huangmei.commonhm.model.AdminUser;
import com.huangmei.commonhm.model.Entity.AdminUserCriteria;
import com.huangmei.commonhm.model.Entity.Value;
import com.huangmei.commonhm.service.AdminUserService;
import com.huangmei.commonhm.util.CryptUtil;
import com.huangmei.commonhm.util.ErrorCode;
import com.huangmei.commonhm.util.JsonResult;
import com.huangmei.commonhm.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class AdminUserServiceImpl extends BaseServiceImpl<Integer, AdminUser> implements AdminUserService {
	


	public JsonResult Login(String passport, String password, HttpServletRequest request) throws Exception {
		JsonResult result = new JsonResult();
		if (StringUtil.isBlank(passport)) {
			result.addErrorCode(ErrorCode.SYS_PARAM_VALUE_ERROR);
			return result;
		}
		if (StringUtil.isBlank(password)) {
			result.addErrorCode(ErrorCode.SYS_PARAM_VALUE_ERROR);
			return result;
		}
		
		AdminUserCriteria criteria = new AdminUserCriteria();
		criteria.setUserName(Value.eq(passport));
        criteria.setPassword(Value.eq(password));//CryptUtil.md5(password)
        AdminUser admin = selectOne(criteria);
		if (admin == null) {
			result.addErrorCode(ErrorCode.CUSTOM_USER_PWD_ERROR);
			return result;
		}
		admin.setLastLogin(new Date());
		admin.setLastIp(request.getRemoteAddr());
		update(admin);
		result.setData(admin);
		return result;
	}


	public JsonResult saveOrUpdate(AdminUser admin) {
		JsonResult result = new JsonResult();
		if (admin.getId() == null) {
			// 新增
			admin.setAddTime(new Date());
			admin.setPassword(CryptUtil.md5(admin.getPassword()));
			Integer save = save(admin);
			if (save == null) {
				result.addErrorCode(ErrorCode.SYS_ERR);
				return result;
			}
		}else {
			// 修改
			Integer update = update(admin);
			if (update == null) {
				result.addErrorCode(ErrorCode.SYS_ERR);
				return result;
			}
		}
		return result;
	}

}