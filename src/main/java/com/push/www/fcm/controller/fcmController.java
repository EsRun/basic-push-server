package com.push.www.fcm.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.push.www.fcm.service.fcmService;

@RestController
public class fcmController {

	@Autowired
	fcmService service;
	
	@GetMapping("/sendMsg")
	public void sendMessage() throws IOException {
		fcmService.sendCommonMessage();
	}
	
}
