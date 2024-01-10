package com.push.www.fcm.util;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
public class fcmMessageType {
	public int type;
	
	public fcmMessageType() {}
	
	public fcmMessageType(int type) {
		this.type = type;
	}

	public JsonObject alertType(int type) {
		JsonObject msgTitle = new JsonObject();
		switch (type) {
		case 1: {
			msgTitle.addProperty("1번", "내용");
			break;
		}
		case 2:{
			msgTitle.addProperty("2번", "내용");
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		return msgTitle;
	}
}
