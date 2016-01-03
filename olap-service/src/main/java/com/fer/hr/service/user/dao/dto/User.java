package com.fer.hr.service.user.dao.dto;

public class User {
	private String email;
	private String saltedHashPassword;
	private String auhenticationToken;
	
	public User(String email, String saltedHashPassword) {
		super();
		this.email = email;
		this.saltedHashPassword = saltedHashPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSaltedHashPassword() {
		return saltedHashPassword;
	}

	public void setSaltedHashPassword(String saltedHashPassword) {
		this.saltedHashPassword = saltedHashPassword;
	}

	public String getAuhenticationToken() {
		return auhenticationToken;
	}

	public void setAuhenticationToken(String auhenticationToken) {
		this.auhenticationToken = auhenticationToken;
	}
	
}
