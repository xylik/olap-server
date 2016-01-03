package com.fer.hr.web.service.security.dto;

public class UserToken {
	private String userEmail;
	private String authenticationToken;

	public UserToken() {
	}
	
	public UserToken(String userEmail, String authenticationToken) {
		super();
		this.userEmail = userEmail;
		this.authenticationToken = authenticationToken;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

}
