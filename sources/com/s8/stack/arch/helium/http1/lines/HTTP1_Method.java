package com.s8.stack.arch.helium.http1.lines;


import java.util.HashMap;
import java.util.Map;

public enum HTTP1_Method {
	
	CONNECT("CONNECT", "Connect"),
	DELETE("DELETE", "delete"),
	GET("GET", "get"),
	HEAD("HEAD", "head"),
	OPTIONS("OPTIONS", "Options"),
	PATCH("PATCH", "Patch"),
	POST("POST", "Post"),
	PUT("PUT", "Put"),
	HOST("HOST", "Host");
	
	private String[] aliases;
	
	private HTTP1_Method(String... aliases) {
		this.aliases = aliases;
	}
	
	private static Map<String, HTTP1_Method> MAP;
	
	
	public static HTTP1_Method get(String key) {
		if(MAP==null) {
			MAP = new HashMap<String, HTTP1_Method>();
			for(HTTP1_Method method : HTTP1_Method.values()){
				for(String alias : method.aliases){
					MAP.put(alias, method);
				}
			}
		}
		return MAP.get(key);
	}
	
}
