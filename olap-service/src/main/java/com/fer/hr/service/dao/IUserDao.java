package com.fer.hr.service.dao;

import java.util.HashMap;

import com.fer.hr.service.dao.dto.User;

public interface IUserDao {
	
	HashMap<String, User> getUsers();
	
	User createUser(String email, String password);
	
}
