package com.s8.stack.arch.helium.http2;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>Source: Mozilla Development Network (as of October 2019)</p>
 * 
 * @author pc
 *
 */
public enum HTTP2_Method {
	
	CONNECT("connect", false, "CONNECT", "Connect"),
	DELETE("delete", false, "DELETE", "Delete"),
	GET("get", false, "GET", "Get"),
	HEAD("head", false, "HEAD", "Head"),
	OPTIONS("options", false, "OPTIONS", "Options"),
	TRACE("trace", false, "TRACE", "Trace"),
	PATCH("patch", false, "PATCH", "Patch"),
	POST("post", true, "POST", "Post"),
	PUT("put", false, "PUT", "Put");

	public boolean isRequiringData;
	
	public String keyword;
	
	public String[] aliases;
	
	private HTTP2_Method(String keyword, boolean isRequiringData, String... aliases) {
		this.keyword = keyword;
		this.isRequiringData = isRequiringData;
		this.aliases = aliases;
	}
	
	
	private static Map<String, HTTP2_Method> mapping;

	public static HTTP2_Method get(String tag) {
		if(mapping==null) {
			mapping = new HashMap<String, HTTP2_Method>();
			for(HTTP2_Method method : HTTP2_Method.values()){
				mapping.put(method.keyword, method);
				for(String alias : method.aliases){
					mapping.put(alias, method);
				}
			}
		}
		return mapping.get(tag);
	}
		
}
