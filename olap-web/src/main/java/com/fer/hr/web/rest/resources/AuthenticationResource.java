package com.fer.hr.web.rest.resources;

import java.io.Serializable;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fer.hr.web.rest.util.AuthenticationUtil;
import com.fer.hr.web.service.AuthenticationService;
import com.fer.hr.web.service.dto.UserToken;

@Component
@Path("/authentication")
public class AuthenticationResource implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int EMAIL_POS = 0;
	private static final int PASSWORD_POS = 1;
	public static final String USER_EXIST = "User with provided email allready exist!";
	public static final String INVALID_AUTHENTICATION = "Invalid user name or password!";
	public static final String LOGOUT_OK = "Successfully logged out!";
	
	private AuthenticationService authenticationService;
	
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@POST
    @Consumes("application/x-www-form-urlencoded")
	@Path("/register")
	public Response register(
			@FormParam("credentials") String credentials,
			@FormParam("gcmid")String gcmId) {
		
		String authToken = generateAuthenticationToken(credentials, gcmId);

		if(authToken == null) return Response.status(Status.CONFLICT).entity(USER_EXIST).type(MediaType.TEXT_PLAIN).build();
		else return Response.ok(authToken).type(MediaType.TEXT_PLAIN).build();
	}
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/login")
	public Response login(@FormParam("credentials") String credentials) {
		
		String authToken = generateAuthenticationToken(credentials, null);
		
		if(authToken == null) return Response.status(Status.FORBIDDEN).entity(INVALID_AUTHENTICATION).type(MediaType.TEXT_PLAIN).build();
		else return Response.ok(authToken).type(MediaType.TEXT_PLAIN).build();
	}
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/logout")
	public Response logout(@FormParam("authenticationToken")String token) {
		boolean isLogedOut = authenticationService.logout(token);

		if(isLogedOut) return Response.ok(LOGOUT_OK).build();
		else return Response.status(Status.FORBIDDEN).build();
	}
	
	private String generateAuthenticationToken(String credentials, String gcmId) {
		String[] decodedCredentials = AuthenticationUtil.decodeCredentials(credentials);
		String userEmail = decodedCredentials[EMAIL_POS];
		String userPassword = decodedCredentials[PASSWORD_POS];
		
		UserToken token = null;
		if(StringUtils.isEmpty(gcmId)) token = authenticationService.login(userEmail, userPassword);
		else token = authenticationService.register(userEmail, userPassword, gcmId);
		
		return token == null ? null : token.getAuthenticationToken();
	}
	
}
