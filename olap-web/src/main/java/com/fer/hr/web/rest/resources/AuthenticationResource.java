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

import com.fer.hr.service.security.AuthenticationService;
import com.fer.hr.service.security.UserToken;

@Component
@Path("/authentication")
public class AuthenticationResource implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String USER_EXIST = "User with provided email allready exist!";
	public static final String INVALID_AUTHENTICATION = "Invalid user name or password!";
	public static final String LOGOUT_OK = "Succesfully loged out!";
	
	private AuthenticationService authenticationService;
	
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@POST
    @Consumes("application/x-www-form-urlencoded")
	@Path("/register")
	public Response register(@FormParam("email") String userEmail, @FormParam("password") String userPassword) {
		UserToken token = authenticationService.register(userEmail, userPassword);
		
		if(token == null) return Response.status(Status.CONFLICT).entity(USER_EXIST).type(MediaType.TEXT_PLAIN).build();
		else return Response.ok(token).type(MediaType.APPLICATION_JSON).build();
	}
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/login")
	public Response login(@FormParam("email") String userEmail, @FormParam("password")String userPassword) {
		UserToken token = authenticationService.login(userEmail, userPassword);
		
		if(token == null) return Response.status(Status.FORBIDDEN).entity(INVALID_AUTHENTICATION).type(MediaType.TEXT_PLAIN).build();
		else return Response.ok(token).type(MediaType.APPLICATION_JSON).build();
	}
	
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Path("/logout")
	public Response logout(@FormParam("authenticationToken")String token) {
		boolean isLogedOut = authenticationService.logout(token);

		if(isLogedOut) return Response.ok(LOGOUT_OK).build();
		else return Response.status(Status.FORBIDDEN).build();
	}
	
}
