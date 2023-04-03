package com.s8.stack.arch.helium.http2.headers;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author pc
 *
 */
public class HTTP2_HeaderMapping {

	

	private Map<String, HTTP2_Header.Prototype> map;

	private boolean isVerbose;

	
	/**
	 * 
	 * @param isVerbose
	 */
	public HTTP2_HeaderMapping(boolean isVerbose) {
		super();
		this.isVerbose = isVerbose;
		initialize();
	}

	


	private void initialize() {
		map = new HashMap<String, HTTP2_Header.Prototype>(HTTP2_Header.PROTOTYPES.length);
		for(HTTP2_Header.Prototype prototype : HTTP2_Header.PROTOTYPES) {
			for(String name : prototype.names) {
				map.put(name, prototype);	
			}
		}
	}

	/**
	 * Always return a header
	 * @param name
	 * @return
	 */
	public HTTP2_Header.Prototype get(String name) {
		HTTP2_Header.Prototype type = map.get(name);
		if(type!=null) {
			return type;
		}
		else {
			if(isVerbose) {
				System.out.println("[HTTP2_HeaderMapping] "+
						"Cannot resolve header name: "+name+" -> mapping to x-unmapped");	
			}
			return Unmapped.PROTOTYPE;
		}
	}


	/**
	 * Create header
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public HTTP2_Header createHeader(String name, String value) {
		return get(name).parse(value);
	}

	public HTTP2_Header createHeader(String name) {
		return get(name).create();
	}


}
