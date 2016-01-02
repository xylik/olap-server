package com.fer.hr.service.security.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecurityUtil {
	private static SecureRandom random = new SecureRandom();
	private static final String SALT = "123456789";

	public static String generateToken() {
		return new BigInteger(130, random).toString(32);
	}
	
	public static String getSaltedHash(String text) {
		return getHash(SALT + text);
	}
	
	public static String getHash(String text) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		m.reset();
		m.update(text.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
		  hashtext = "0"+hashtext;
		}
		return hashtext;
	}
}
