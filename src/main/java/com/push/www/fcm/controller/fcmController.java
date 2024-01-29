package com.push.www.fcm.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.push.www.fcm.service.fcmService;

@RestController
public class fcmController {

	fcmService service;

	@Autowired
	public fcmController(fcmService service) {
		this.service = service;
	}
	
	@PostMapping("/sendMsg")
	public void sendMessage(@RequestBody Map<String, String> body
			//@RequestParam(name = "fcmTitle") String fcmTitle,
			//@RequestParam(name = "fcmBody") String fcmBody
			) throws IOException {
		
		fcmService.sendCommonMessage(body.get("fcmTitle"), body.get("fcmBody"));
	}
	
}
