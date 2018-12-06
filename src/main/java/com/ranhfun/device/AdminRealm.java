package com.ranhfun.device;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ranhfun.device.domain.User;
import com.ranhfun.device.service.UserRepository;
import com.ranhfun.device.service.UtilManager;


public class AdminRealm extends AuthorizingRealm {

	private static Logger logger = LoggerFactory.getLogger(AdminRealm.class);
	
	private UserRepository userRepository;
	private UtilManager utilManager;
	
    public AdminRealm(UserRepository userRepository, UtilManager utilManager) {
    	this.userRepository = userRepository;
    	this.utilManager = utilManager;
    }
    
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String email = upToken.getUsername();

        if (email == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        AuthenticationInfo info = null;
        User user = userRepository.findByEmail(email);
		if (user==null) {
			final String message = "There was a SQL error while authenticating user [" + email + "]";
		    if (logger.isErrorEnabled()) {
		        logger.error(message);
		    }

		    throw new AuthenticationException(message);
		}
		utilManager.setUserId(user.getId());
		utilManager.setUserName(user.getName());
//		Subject subject = SecurityUtils.getSubject();
//        Session session = subject.getSession();
//        session.setAttribute(UtilManager.USER_ID, );
//        session.setAttribute(UtilManager.USER_NAME, );
		String password = user.getPassword();
		info = buildAuthenticationInfo(email, password.toCharArray()); 

        return info;
    }

    protected AuthenticationInfo buildAuthenticationInfo(String email, char[] password) {
        return new SimpleAuthenticationInfo(email, password, getName());
    }

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.addRole("*");
		info.addStringPermission("*");
		return info;
	}
}

