package com.huangmei.commonhm.service;

import javax.servlet.http.HttpServletRequest;

import com.huangmei.commonhm.model.AdminUser;
import com.huangmei.commonhm.util.JsonResult;

public interface AdminUserService extends BaseService<Integer, AdminUser> {

	JsonResult Login(String passport, String password, HttpServletRequest request)  throws Exception;

	JsonResult saveOrUpdate(AdminUser admin);
}