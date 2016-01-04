package com.fer.hr.service.user.dao;

import java.util.HashMap;

import com.fer.hr.service.user.dao.dto.User;

public interface IUserDao {
	
	HashMap<String, User> getUsers();
	
	User createUser(String email, String password, String gcmId);
	
}
