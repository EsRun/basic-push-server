package com.push.www.fcm.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.protobuf.Extension.MessageType;
import com.push.www.fcm.util.fcmMessageType;

@Service
public class fcmService {

	public fcmMessageType fcmType;
	
	private static final String PROJECT_ID = "mungyeong-b77e4";
	private static final String BASE_URL = "https://fcm.googleapis.com";
	private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";

	private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
	private static final String[] SCOPES = { MESSAGING_SCOPE };

	private static final String TITLE = "FCM Notification";
	private static final String BODY = "Notification from FCM";
	public static final String MESSAGE_KEY = "message";

	@Autowired
	public fcmService(fcmMessageType fcmType) {
		this.fcmType = fcmType;
	}
	
	/**
	 * Retrieve a valid access token that can be use to authorize requests to the
	 * FCM REST API.
	 *
	 * @return Access token.
	 * @throws IOException
	 */
	// [START retrieve_access_token]
	private static String getAccessToken() throws IOException {
		Resource resource = new ClassPathResource("config/mungyeong-firebase-adminsdk.json");
		InputStream serviceAccount = resource.getInputStream();
		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount).createScoped(Arrays.asList(SCOPES));

		googleCredentials.refreshIfExpired();
        return googleCredentials.refreshAccessToken().getTokenValue();
	}

	/**
	 * Create HttpURLConnection that can be used for both retrieving and publishing.
	 *
	 * @return Base HttpURLConnection.
	 * @throws IOException
	 */
	private static HttpURLConnection getConnection() throws IOException {
		
		String headerAuth = "Bearer " + getAccessToken();
		
		// [START use_access_token]
		URL url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setRequestProperty("Authorization", headerAuth);
		httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
		System.out.println(httpURLConnection.getRequestProperty("Authorization"));
		return httpURLConnection;
		// [END use_access_token]
	}

	/**
	 * Send request to FCM message using HTTP. Encoded with UTF-8 and support
	 * special characters.
	 *
	 * @param fcmMessage Body of the HTTP request.
	 * @throws IOException
	 */
	private static void sendMessage(JsonObject fcmMessage) throws IOException {
		HttpURLConnection connection = getConnection();
		connection.setDoOutput(true);
		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
		writer.write(fcmMessage.toString());
		writer.flush();
		writer.close();

		int responseCode = connection.getResponseCode();
		if (responseCode == 200) {
			String response = inputstreamToString(connection.getInputStream());
			System.out.println("Message sent to Firebase for delivery, response:");
			System.out.println(response);
		} else {
			System.out.println("Unable to send message to Firebase:");
			String response = inputstreamToString(connection.getErrorStream());
			System.out.println(response);
		}
	}

	/**
	 * Send a message that uses the common FCM fields to send a notification message
	 * to all platforms. Also platform specific overrides are used to customize how
	 * the message is received on Android and iOS.
	 *
	 * @throws IOException
	 */
	private static void sendOverrideMessage() throws IOException {
		JsonObject overrideMessage = buildOverrideMessage();
		System.out.println("FCM request body for override message:");
		prettyPrint(overrideMessage);
		sendMessage(overrideMessage);
	}

	/**
	 * Build the body of an FCM request. This body defines the common notification
	 * object as well as platform specific customizations using the android and apns
	 * objects.
	 *
	 * @return JSON representation of the FCM request body.
	 */
	private static JsonObject buildOverrideMessage() {
		JsonObject jNotificationMessage = buildNotificationMessage("", "");

		JsonObject messagePayload = jNotificationMessage.get(MESSAGE_KEY).getAsJsonObject();
		messagePayload.add("android", buildAndroidOverridePayload());

		JsonObject apnsPayload = new JsonObject();
		apnsPayload.add("headers", buildApnsHeadersOverridePayload());
		apnsPayload.add("payload", buildApsOverridePayload());

		messagePayload.add("apns", apnsPayload);

		jNotificationMessage.add(MESSAGE_KEY, messagePayload);

		return jNotificationMessage;
	}

	/**
	 * Build the android payload that will customize how a message is received on
	 * Android.
	 *
	 * @return android payload of an FCM request.
	 */
	private static JsonObject buildAndroidOverridePayload() {
		JsonObject androidNotification = new JsonObject();
		androidNotification.addProperty("click_action", "android.intent.action.MAIN");

		JsonObject androidNotificationPayload = new JsonObject();
		androidNotificationPayload.add("notification", androidNotification);

		return androidNotificationPayload;
	}

	/**
	 * Build the apns payload that will customize how a message is received on iOS.
	 *
	 * @return apns payload of an FCM request.
	 */
	private static JsonObject buildApnsHeadersOverridePayload() {
		JsonObject apnsHeaders = new JsonObject();
		apnsHeaders.addProperty("apns-priority", "10");

		return apnsHeaders;
	}

	/**
	 * Build aps payload that will add a badge field to the message being sent to
	 * iOS devices.
	 *
	 * @return JSON object with aps payload defined.
	 */
	private static JsonObject buildApsOverridePayload() {
		JsonObject badgePayload = new JsonObject();
		badgePayload.addProperty("badge", 1);

		JsonObject apsPayload = new JsonObject();
		apsPayload.add("aps", badgePayload);

		return apsPayload;
	}

	/**
	 * Send notification message to FCM for delivery to registered devices.
	 *
	 * @throws IOException
	 */
	public static void sendCommonMessage(String fcmTitle, String fcmBody) throws IOException {

		JsonObject notificationMessage = buildNotificationMessage(fcmTitle, fcmBody);
		System.out.println("FCM request body for message using common notification object:");
		prettyPrint(notificationMessage);
		sendMessage(notificationMessage);
	}

	/**
	 * Construct the body of a notification message request.
	 *
	 * @return JSON of notification message.
	 */
	private static JsonObject buildNotificationMessage(String fcmTitle, String fcmBody) {
		JsonObject jNotification = new JsonObject();
		jNotification.addProperty("title", fcmTitle);
		jNotification.addProperty("body", fcmBody);
		
		//jNotification = fcmType.alertType(fcmTitle, fcmBody);
		
		JsonObject jMessage = new JsonObject();
		jMessage.add("notification", jNotification);
		final String tokens = "e5wTBpJ_SC2jzBhTgeJtcE:APA91bGwtx7PiOgb_7mVYo4x9GKkbDygHeTvk48CQROP_xPZU5Kor0VUqZoXucl3dhZZDY6pv53cjoIZTIIevPlINCPwakDz9i6lVQ-rpbQwU-g5DZj0xnJunKFfYXB-MPDTZvOkoV3o";
		final String aaa = "ezGPwgzmSmSzCcaAxHtPCg:APA91bE3skSmy4xyRoshKQpIPrUdx9_xhJT39tnnwlrJ-cX29v3cKmKOGwbZZ28lv6snrRkkVpKyDvlJWLE0bHcJQ-W0D2K3jtLqBEkJ5lg5kv_Q8O0NqrnxjGPemzKfBOFlIU3gkYbg";
		final String aa = "fBHwUNyOSqmVqHybPdFYmD:APA91bGXOCvQU8BR9x7-v99yJxCCMKr5GhReGABYVPmMaQeeM5WDcDkwmt9GEx99Ekwdt4jyytN-WNxEPbYxNxqje9B90lV4dDOE9zVSpRKIRhgf7LVXyhDFnZGxcgEfNEEymfqznAXF";
		final String G23_token = "eedYAfJUQ_-31CBCE01yv-:APA91bFD4HFfm1Ogc8jj-RJvQ6k3BkURc480Is3PvwT8aEl3l3gRgcEYVvAPSMUscbgOW_yuVoVXOrrrbOym3aLsGLwU55b_7eQCDqDOm8_1q7UcTk7EBo6CM_mgkQGCUcVWcNkOxhSF";
		//jMessage.addProperty("topic", "test");
		jMessage.addProperty("token", aa);
		JsonObject jFcm = new JsonObject();
		jFcm.add(MESSAGE_KEY, jMessage);

		return jFcm;
	}

	/**
	 * Read contents of InputStream into String.
	 *
	 * @param inputStream InputStream to read.
	 * @return String containing contents of InputStream.
	 * @throws IOException
	 */
	private static String inputstreamToString(InputStream inputStream) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		Scanner scanner = new Scanner(inputStream);
		while (scanner.hasNext()) {
			stringBuilder.append(scanner.nextLine());
		}
		return stringBuilder.toString();
	}

	/**
	 * Pretty print a JsonObject.
	 *
	 * @param jsonObject JsonObject to pretty print.
	 */
	private static void prettyPrint(JsonObject jsonObject) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(gson.toJson(jsonObject) + "\n");
	}

}
