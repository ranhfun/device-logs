package com.ranhfun.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ranhfun.device.service.LogInfoRepository;


@Controller
public class LogInfoController {

	@Autowired
	private LogInfoRepository logInfoRepository;
	
	@GetMapping("/log-info")
    public String showUsers(Model model) {
		model.addAttribute("logInfos", logInfoRepository.findAll());
        return "log-info";
    }
}
