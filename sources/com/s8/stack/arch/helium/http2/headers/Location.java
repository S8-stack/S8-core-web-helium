package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.RESPONSE;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierre convert
 * 
 */
public class Location extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x43,
			new String[] { "location" }, 
			false,
			RESPONSE, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Location(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Location();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.location;
		}
	};

	
	public String value;

	public Location() {
		super();
	}
	
	public Location(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.location = this;
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String value) {
		this.value = value;
	}
}
