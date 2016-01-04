package com.fer.hr.web.service;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import com.fer.hr.service.user.UserService;
import com.fer.hr.service.user.dao.dto.User;
import com.fer.hr.service.util.SecurityUtil;
import com.fer.hr.web.service.dto.UserToken;

public class AuthenticationService {
	
	private HashMap<String, String> sessionsByTokenKey = new HashMap<>();
	private HashMap<String, String> sessionsByEmailKey = new HashMap<>();
	private UserService userService;
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public UserToken login(String userEmail, String userPassword) {
		if(StringUtils.isEmpty(userEmail) || StringUtils.isEmpty(userPassword)) return null;
		
		String sessionToken = sessionsByEmailKey.get(userEmail);
		User user =userService.getUserForEmail(userEmail);
		String saltedPasswordHash = SecurityUtil.getSaltedHash(userPassword);
		
		if( user != null && user.getSaltedHashPassword().equals(saltedPasswordHash)) {
			if(sessionToken != null) return new UserToken(userEmail, sessionToken);
			else return new UserToken(userEmail, createSession(userEmail));
		}
		else return null;
	}
	
	public boolean logout(String sessionToken) {
		if( !isUserLogedIn(sessionToken) ) return false;

		String userEmail = sessionsByTokenKey.get(sessionToken);
		sessionsByTokenKey.remove(sessionToken);
		sessionsByEmailKey.remove(userEmail);
		return true;
	}
	
	public UserToken register(String userEmail, String userPassword, String gcmId) {
		if(StringUtils.isEmpty(userEmail) && StringUtils.isEmpty(userPassword)) return null;
		else if( userService.addUser(userEmail, userPassword, gcmId) == null) return null;
		else return login(userEmail, userPassword);
	}
	
	public boolean isUserLogedIn(String authenticationToken) {
		if(StringUtils.isEmpty(authenticationToken))
			return false;
		else 
			return sessionsByTokenKey.get(authenticationToken) != null ? true : false;
	}
	
	private String createSession(String userEmail) {		
		String sessionToken = SecurityUtil.generateToken();
		sessionsByTokenKey.put(sessionToken, userEmail);
		sessionsByEmailKey.put(userEmail, sessionToken);
		return sessionToken;
	}
	
}
