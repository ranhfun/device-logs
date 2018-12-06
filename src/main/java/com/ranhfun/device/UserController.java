package com.ranhfun.device;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.web.util.SavedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ranhfun.device.service.UserRepository;

@Controller
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private StringRedisTemplate template;
	
	@Autowired
	private RedisKeyValueTemplate template2;
	
	@RequiresPermissions("test")
	//@RequiresRoles("test")
	@GetMapping("/user")
    public String showUsers(HttpServletRequest request, Model model) {
		model.addAttribute("users", userRepository.findAll());
		model.addAttribute("hostName", getHostName());
//		ValueOperations<String, String> ops = this.template.opsForValue();
//		String key = "spring.boot.redis.test";
//		if (!this.template.hasKey(key)) {
//			ops.set(key, "foo");
//		}
//		System.out.println("Found key " + key + ", value=" + ops.get(key));
//		String uuid = UUID.randomUUID().toString();
//		SimpleSession session = new SimpleSession();
//		session.setId(uuid);
//		session.setAttribute("saverequest", new SavedRequest(request));
//		template2.insert(uuid, session);
//		System.out.println("Found key " + uuid + ", value=" + template2.findById(uuid, SimpleSession.class));
        return "user";
    }
	
	
	@GetMapping("/user.delete")
    public String showUsers(@RequestParam("userId")Integer userId, Model model) {
		userRepository.deleteById(userId);
        return "redirect:/user";
    }
	
	public String getHostName() {
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			return addr.getHostAddress();
		} catch (UnknownHostException e) {
			//LOG.error("Hostname can not be resolved", e);
			return null;
		}
	}
}
