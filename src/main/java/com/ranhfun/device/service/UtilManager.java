package com.ranhfun.device.service;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UtilManager {

	private static final Logger log = LoggerFactory.getLogger(UtilManager.class);
	
	public static final String USER_ID = "USER_ID";
	public static final String USER_NAME = "USER_NAME";
	
	public Integer getUserId() {
		return Integer.valueOf(getValue(USER_ID));
	}
	
	public void setUserId(Integer userId) {
		setValue(USER_ID, userId.toString());
	}
	
	public String getUserName() {
		return getValue(USER_NAME);
	}
	
	public void setUserName(String userName) {
		setValue(USER_NAME, userName);
	}
	
	private String getValue(String key) {
		String value = null;

        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
			return null;
		}
        Session session = subject.getSession(false);
        if (session != null) {
            value = (String)session.getAttribute(key);
            if (log.isDebugEnabled()) {
                log.debug("retrieving session key [" + key + "] with value [" + value + "] on session with id [" + session.getId() + "]");
            }
        }

        return value;
	}
	
    private void setValue(String key, String newValue) {
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();

        if (log.isDebugEnabled()) {
            log.debug("saving session key [" + key + "] with value [" + newValue + "] on session with id [" + session.getId() + "]");
        }

        session.setAttribute(key, newValue);
    }
}
