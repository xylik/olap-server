package com.fer.hr.web.rest.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.fer.hr.web.service.GCMService;

@Component
@Provider
@Path("/gcm")
public class GCMResources {
	public static final String SUCCESS_MSG = "all_sent";
	public static final String FAIL_MSG = "some_sent";
	
	private GCMService gcmService;
	
	public void setGcmService(GCMService gcmService) {
		this.gcmService = gcmService;
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/message/push")
	public Response sendPushNotification(
			@FormParam("messageText")String messageText, 
			@FormParam("emails")String emailsConcatenated) {
		
		String[] emails =  emailsConcatenated.split(";");
		Response badRequest = Response.status(Status.BAD_REQUEST).build();
		
		if(StringUtils.isEmpty(messageText) && StringUtils.isEmpty(messageText)) return badRequest;
		else if(emails == null || emails.length == 0) return badRequest;

		boolean isSent = gcmService.sendPushNotification(messageText, emails);
		if(isSent) return Response.ok(SUCCESS_MSG).build();
		else return Response.ok(FAIL_MSG).build();
	}

}
