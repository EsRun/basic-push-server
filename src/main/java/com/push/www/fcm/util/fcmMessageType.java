package com.push.www.fcm.util;

public class fcmMessageType {
	public int type;
	
	public fcmMessageType(int type) {
		this.type = type;
	}
	
	public String fcmSelectType(int type) {
		String msgTitle;
		switch (type) {
		case 1: {
			msgTitle = "1번 타입";
		}
		case 2:{
			msgTitle = "2번 타입";
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		return msgTitle;
	}
}
