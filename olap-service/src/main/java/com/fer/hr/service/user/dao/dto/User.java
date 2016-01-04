package com.fer.hr.service.user.dao.dto;

public class User {
	private String email;
	private String saltedHashPassword;
	private String gcmId;
	
	
	public User(String email, String saltedHashPassword, String gcmId) {
		super();
		this.email = email;
		this.saltedHashPassword = saltedHashPassword;
		this.gcmId = gcmId;
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

	public String getGcmId() {
		return gcmId;
	}

	public void setGcmId(String gcmId) {
		this.gcmId = gcmId;
	}
}
