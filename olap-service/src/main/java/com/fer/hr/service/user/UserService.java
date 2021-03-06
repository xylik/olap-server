package com.fer.hr.service.user;

import com.fer.hr.service.user.dao.IUserDao;
import com.fer.hr.service.user.dao.dto.User;

public class UserService {
	private IUserDao userDao;

	public void setUserDao(IUserDao userDao) {
		this.userDao = userDao;
	}

	public User addUser(String email, String password, String gcmId) {
		return userDao.createUser(email, password, gcmId);
	}

	public User getUserForEmail(String email) {
		return userDao.getUsers().get(email);
	}

}
