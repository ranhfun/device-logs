package com.ranhfun.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ranhfun.device.domain.User;
import com.ranhfun.device.service.UserRepository;

@Controller
public class RegisterController {

	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/register")
    public String showRegister(User user, @RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "register";
    }
	
	@PostMapping("/register")
    protected String onSubmit(User user,
                              Model model) throws Exception {
		userRepository.save(user);
        return "redirect:/user";
    }
	
}
