package com.fer.hr.web.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.fer.hr.service.user.UserService;
import com.fer.hr.service.user.dao.dto.User;
import com.fer.hr.web.service.gcm.InvalidRequestException;
import com.fer.hr.web.service.gcm.Message;
import com.fer.hr.web.service.gcm.MulticastResult;
import com.fer.hr.web.service.gcm.Sender;

public class GCMService {
	private static String SERVER_GCM_KEY;
	private static final String PROPERTIES_FILE = "/security.properties";
	public static final int RETRIES = 3;
	public static final int TTL = 30;
	public static final String MSG_KEY = "message";
	
	private UserService userService;
	
	static{
		InputStream is = GCMService.class.getResourceAsStream(PROPERTIES_FILE);
		Properties securityProperties = new Properties();
		try {
			securityProperties.load(is);
			SERVER_GCM_KEY = securityProperties.getProperty("SERVER_GCM_KEY", null);
			is.close();
		}catch(IOException ex) {
			System.err.println(ex.toString());
		}
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public boolean sendPushNotification(String messageText, String[] userEmails ) {
		ArrayList<String> sendToGcmIds = getGcmIds(userEmails);

		Sender sender = new Sender(SERVER_GCM_KEY);
		Message message = new Message.Builder().timeToLive(TTL).delayWhileIdle(false).addData(MSG_KEY, messageText).build();

		try {
			MulticastResult result = sender.send(message, sendToGcmIds, RETRIES);

			if (result.getFailure() > 0) {
				System.out.println(result.getFailure() + " of " + result.getTotal() + " messages not sent!");
				return false;
			}

			System.out.println("GCM Notifications sent successfully!");
			return true;
		} catch (InvalidRequestException e) {
			System.err.println("Invalid Request\n" + e.getStackTrace());
		} catch (IOException e) {
			System.err.println("IO Exception\n" + e.getStackTrace());
		}
		return false;
	}
	
	private ArrayList<String> getGcmIds(String[] userEmails) {
		ArrayList<String> sendToList = new ArrayList<>();
		
		for(String email : userEmails) {
			User u = userService.getUserForEmail(email);
			if(u != null) sendToList.add(u.getGcmId());
		}
		return sendToList;
	}

}
