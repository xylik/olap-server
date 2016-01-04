package com.fer.hr.web.rest.util;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.StringTokenizer;

public class AuthenticationUtil {
	private static Decoder base64Decoder;
	
	static{
		base64Decoder = Base64.getDecoder();
	}

	private AuthenticationUtil() {}
	
	public static String[] decodeCredentials(String credentials) {
		final String encodedUserPassword = credentials.replaceFirst("Basic ", "");
		String usernameAndPassword = null;
		try {
			byte[] decodedBytes = base64Decoder.decode(encodedUserPassword);
			usernameAndPassword = new String(decodedBytes, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
		final String userEmail = tokenizer.nextToken();
		final String userPassword = tokenizer.nextToken();
		
		return new String[]{userEmail, userPassword};
	}

}
