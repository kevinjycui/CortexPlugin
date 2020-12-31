package com.junferno.fear.emotiv;

import java.net.URI;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

class CortexSession {
	private String id;

	public CortexSession(String id) {
		this.id = id;
	}

	public String toString() {
		return this.id;
	}
}

public class CortexHandler {

	private static final String ENDPOINT = "wss://localhost:6868";
	private JSONObject credentials;
	private String token = null;
	private String headsetId = null;
	private CortexSession session = null;

	protected WebSocketEmotivClient client;

	public CortexHandler() {
		try {
			this.credentials = JSONHandler.readJSONFile(Paths.get("cortex", "credentials.json").toString());
			this.client = new WebSocketEmotivClient(new URI(ENDPOINT));

			// This trust manager trusts all hosts, not recommended for production
			TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustManager, new java.security.SecureRandom());

			SSLSocketFactory factory = sslContext
					.getSocketFactory();

			this.client.setSocketFactory(factory);

			this.client.connectBlocking();

			this.setup();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void setup() throws Exception {

		// Getting Cortex info as test
		System.out.println("Getting Cortex info...");
		this.getCortexInfo();
		while (this.client.loopWaited()) Thread.sleep(1);

		// Requesting access from Cortex App (check app for request)
		System.out.println("Requesting access...");
		this.requestAccess();
		while (this.client.loopWaited());

		boolean code = true;

		// Querying headsets for headset ID
		System.out.println("Querying headsets...");
		this.queryHeadsets();
		while (this.client.loopWaited());
		if (((JSONArray) this.getResponse().get("result")).size() == 0)
			System.out.println("No headsets detected.");
		this.headsetId = (String) ((JSONObject) ((JSONArray) this.getResponse().get("result")).get(0)).get("id");
		System.out.println("Your headset ID is " + this.headsetId + ".");
		boolean connected = !((String) ((JSONObject) ((JSONArray) this.getResponse().get("result")).get(0)).get("status"))
				.equals("discovered");

		if (!connected) {
			System.out.println("Connecting device...");
			code = this.connectDevice();
			while (code && this.client.loopWaited());
		}

		// Authorizing to generate Cortex token
		System.out.println("Authorizing...");
		this.authorize();
		while (this.client.loopWaited());
		this.token = (String) ((JSONObject) this.getResponse().get("result")).get("cortexToken");
		System.out.println("Your token is " + this.token + ". Do not share this token!");

		// Creating an open session
		System.out.println("Creating open session...");
		code = this.createOpenSession();
		while (code && this.client.loopWaited());
		this.session = new CortexSession((String) ((JSONObject) this.getResponse().get("result")).get("id"));

		// Activating session
		System.out.println("Updating session to active...");
		code = this.updateActiveSession();
		while (code && this.client.loopWaited());

		// Subscribe to metric data
		System.out.println("Subscribing to metric data...");
		JSONArray streams = new JSONArray();
		streams.add("met");
		code = this.subscribe(streams);
		while (code && this.client.loopWaited());

	}
	
	public void open() throws InterruptedException {
		this.client.reconnect();
	}
	
	public void close() throws InterruptedException {
		this.client.closeBlocking();
	}

	public JSONObject getResponse() {
		return this.client.getResponse();
	}

	public void resetResponse() {
		this.client.resetResponse();
	}

	@SuppressWarnings("unchecked")
	public void requestWithCreds(String method) throws InterruptedException {
		this.client.send(JSONHandler.encodeCortexRequest(method, this.credentials).toString());
	}

	public void getCortexInfo() {
		this.client.send(JSONHandler.encodeCortexRequest("getCortexInfo", null).toString());
	}

	@SuppressWarnings("unchecked")
	public void requestAccess() throws InterruptedException {
		this.requestWithCreds("requestAccess");
	}

	@SuppressWarnings("unchecked")
	public void hasAccessRight() throws InterruptedException {
		this.requestWithCreds("hasAccessRight");
	}

	@SuppressWarnings("unchecked")
	public void authorize() throws InterruptedException {
		this.requestWithCreds("authorize");
	}

	public void queryHeadsets() {
		this.client.send(JSONHandler.encodeCortexRequest("queryHeadsets", null).toString());
	}

	@SuppressWarnings("unchecked")
	public boolean connectDevice() {
		if (this.headsetId == null)
			return false;

		JSONObject params = new JSONObject();
		params.put("command", "connect");
		params.put("headset", this.headsetId);

		this.client.send(JSONHandler.encodeCortexRequest("controlDevice", params).toString());
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean createOpenSession() {
		if (this.token == null || this.headsetId == null)
			return false;

		JSONObject params = new JSONObject();
		params.put("cortexToken", this.token);
		params.put("headset", this.headsetId);
		params.put("status", "open");

		this.client.send(JSONHandler.encodeCortexRequest("createSession", params).toString());
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean updateActiveSession() {
		if (this.token == null || this.session == null)
			return false;

		JSONObject params = new JSONObject();
		params.put("cortexToken", this.token);
		params.put("session", this.session.toString());
		params.put("status", "active");

		this.client.send(JSONHandler.encodeCortexRequest("updateSession", params).toString());
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean subscribe(JSONArray streams) {
		if (this.token == null || this.session == null)
			return false;

		JSONObject params = new JSONObject();
		params.put("cortexToken", this.token);
		params.put("session", this.session.toString());
		params.put("streams", streams);

		this.client.send(JSONHandler.encodeCortexRequest("subscribe", params).toString());
		return true;		
	}

	public static void main(String agrs[]) {
		new CortexHandler();
	}

}
