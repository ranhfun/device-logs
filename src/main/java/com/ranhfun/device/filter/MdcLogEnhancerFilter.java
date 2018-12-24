package com.ranhfun.device.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.SecurityUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ranhfun.device.service.UtilManager;

@Component
public class MdcLogEnhancerFilter implements Filter {

	@Autowired
	private UtilManager utilManager;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (SecurityUtils.getSubject().isAuthenticated()) {
			MDC.put("userId", "www.SpringBootDev.com" + utilManager.getUserId());
		}
		chain.doFilter(request, response);		
	}

}
