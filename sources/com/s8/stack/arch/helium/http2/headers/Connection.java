package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.STATIC_OVER_CONNECTION;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.GENERAL;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * @author pierreconvert
 * 
 */
public class Connection extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x21,
			new String[] { "connection"}, 
			false,
			GENERAL, 
			STATIC_OVER_CONNECTION) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Connection(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Connection();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.connection;
		}
	};

	

	public String value;

	
	public Connection() {
		super();
	}
	
	public Connection(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.connection = this;
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
