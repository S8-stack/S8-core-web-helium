package com.s8.stack.arch.helium.http1.headers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.s8.stack.arch.helium.http1.messages.HTTP1_Request;
import com.s8.stack.arch.helium.http1.messages.HTTP1_Response;

public abstract class HTTP1_Header {


	public abstract static class Prototype {

		public final int index;

		/**
		 * HTTP name
		 */
		private String name;

		public abstract HTTP1_Header create();

		public Prototype(int code, String keyword) {
			this.index = code;
			this.name = keyword;
		}

		public String getName() {
			return name;
		}
		
		public abstract void set(HTTP1_Request request, HTTP1_Header header);
		
		public abstract HTTP1_Header get(HTTP1_Response response);
		
		public byte[] getBytes() {
			return name.getBytes(StandardCharsets.US_ASCII);
		}
	}
	
	
	public final static Prototype[] PROTOTYPES = new Prototype[] {
			AcceptHeader.PROTOTYPE,
			CacheControl.PROTOTYPE,
			Connection.PROTOTYPE,
			ContentLength.PROTOTYPE,
			Location.PROTOTYPE,
			TransferEncoding.PROTOTYPE
	};

	/**
	 * return type for identification and handling
	 * @return
	 */
	public abstract HTTP1_Header.Prototype getPrototype();

	/**
	 * read from value String
	 * @param value
	 */
	public abstract void parse(String value);

	/**
	 * responsible for closing line
	 * @param outputStream
	 * @throws IOException
	 */
	public abstract String compose() throws IOException;

	/**
	 * 
	 * @param builder
	 */
	public void printSnaphsot(StringBuilder builder) {
		builder.append(getPrototype().getName());
		builder.append(" : ");
		try {
			String value = compose();	
			builder.append(value);
		}
		catch (Exception e) {
			builder.append("[Error]");
		}
	}



	private static Map<String, HTTP1_Header.Prototype> MAP;

	private static void initializeMap() {
		if(MAP == null) {
			MAP = new HashMap<>();
			for(HTTP1_Header.Prototype type : PROTOTYPES){
				MAP.put(new String(type.name), type);
			}		
		}
	}


	/**
	 * HTTP name
	 * @param name
	 * @return
	 */
	public static HTTP1_Header.Prototype get(String name){
		initializeMap();
		return MAP.get(name);
	}


}
