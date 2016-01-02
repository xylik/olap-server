package com.fer.hr.service.user;

import com.fer.hr.service.dao.IUserDao;
import com.fer.hr.service.dao.dto.User;

public class UserService {
	private IUserDao userDao;

	public void setUserDao(IUserDao userDao) {
		this.userDao = userDao;
	}
	
	public User addUser(String email, String password) {
		return userDao.createUser(email, password);
	}
	
	public User getUserForEmail(String email) {
		return userDao.getUsers().get(email);
	}
	
}
