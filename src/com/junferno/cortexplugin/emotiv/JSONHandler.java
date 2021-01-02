package com.junferno.cortexplugin.emotiv;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONHandler {
	
	private static JSONParser jsonParser = new JSONParser();
	
	@SuppressWarnings("unchecked")
	public static JSONObject encodeCortexRequest(String method, HashMap<String, String> params) {
	      JSONObject obj = new JSONObject();

	      obj.put("id", 1);
	      obj.put("jsonrpc", "2.0");
	      obj.put("method", method);
	      
	      if (params != null)
	    	  obj.put("params", params);

	      return obj;
	}
	
	public static JSONObject decodeCortexResponse(String message) {
		try {
			return (JSONObject) jsonParser.parse(message);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONObject readJSONFile(String filename) {
		try {
			JSONObject obj = (JSONObject) jsonParser.parse(new FileReader(filename));
			return obj;
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

}
