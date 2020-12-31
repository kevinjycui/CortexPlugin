package com.junferno.fear.emotiv;

import java.net.URI;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;

public class WebSocketEmotivClient extends WebSocketClient {
	
	private JSONObject response = null;
	private boolean waiting = true;

	public WebSocketEmotivClient(URI serverUri) {
		super(serverUri);
	}
	
	public JSONObject getResponse() {
		return this.response;
	}
	
	private void setResponse(JSONObject object) {
		this.response = object;
	}
	
	public void resetResponse() {
		this.setResponse(null);
	}
	
	public boolean loopWaited() {
		if (!this.waiting) {
			this.waiting = true;
			return false;
		}
		return true;
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println("Cortex WebSocket Connected");
	}

	@Override
	public void onMessage(String message) {
		this.response = JSONHandler.decodeCortexResponse(message);
		this.waiting = false;
		System.out.println(this.response);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("Cortex WebSocket Disconnected");
	}

	@Override
	public void onError(Exception ex) {
		ex.printStackTrace();

	}

}
