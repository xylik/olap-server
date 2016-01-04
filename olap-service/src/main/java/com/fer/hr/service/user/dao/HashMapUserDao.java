package com.fer.hr.service.user.dao;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.fer.hr.service.user.dao.dto.User;
import com.fer.hr.service.util.SecurityUtil;

public class HashMapUserDao implements IUserDao {
	private HashMap<String, User> usersByEmail = new HashMap<>();

	@Override
	public User createUser(String email, String password, String gcmId) {
		if(StringUtils.isEmpty(email) || StringUtils.isEmpty(password) || usersByEmail.get(email) != null) return null;
		
		String saltedHashPassword = SecurityUtil.getSaltedHash(password);
		User u = new User(email, saltedHashPassword, gcmId);
		
		usersByEmail.put(email, u);
		return u;
	}


	@Override
	public HashMap<String, User> getUsers() {
		return new HashMap<>(usersByEmail);
	}
	
}
