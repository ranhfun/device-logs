package com.ranhfun.device;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

	private static transient final Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@GetMapping("/login")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "login";
    }
	
	@PostMapping("/login")
    protected String onSubmit(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              Model model) throws Exception {

        UsernamePasswordToken token = new UsernamePasswordToken(email, password);

        try {
            SecurityUtils.getSubject().login(token);
        } catch (AuthenticationException e) {
            log.debug("Error authenticating.", e);
            model.addAttribute("errorInvalidLogin", "The username or password was not correct.");

            return "login";
        }

        return "redirect:/user";
    }
	
}
