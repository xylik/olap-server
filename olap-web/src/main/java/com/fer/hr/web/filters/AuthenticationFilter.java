package com.fer.hr.web.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.fer.hr.web.service.AuthenticationService;


public class AuthenticationFilter implements javax.servlet.Filter {
	public static final String AUTHORIZATION_HEADER = "Authorization";

	private AuthenticationService authenticationService;

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public void destroy() {
		//do nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		String reqPath = req.getRequestURI().substring(req.getContextPath().length());

		String token = req.getHeader(AUTHORIZATION_HEADER);
		boolean isResourceProtected = false;
		if (reqPath.matches("^/rest/saiku/\\w+/discover.*$")) isResourceProtected = true;
		else if (reqPath.matches("^/rest/saiku/api/query.*$")) isResourceProtected = true;
		else if(reqPath.matches("^/rest/gcm/message/push$")) isResourceProtected = true;

		if (isResourceProtected) {
			boolean isUserLogedIn = authenticationService.isUserLogedIn(token);
			if (isUserLogedIn)
				filterChain.doFilter(request, response);
			else {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		} else {
			filterChain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(cfg.getServletContext());
		this.authenticationService = ctx.getBean(AuthenticationService.class);
	}

}
