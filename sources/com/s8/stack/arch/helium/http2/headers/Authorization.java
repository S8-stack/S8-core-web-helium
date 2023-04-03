package com.s8.stack.arch.helium.http2.headers;

import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderRefresh.ALWAYS_RENEWED;
import static com.s8.stack.arch.helium.http2.headers.HTTP2_HeaderTarget.REQUEST;

import com.s8.stack.arch.helium.http2.messages.HTTP2_Message;

/**
 * 
 * @author pierreconvert
 */
public class Authorization extends HTTP2_Header {

	public final static Prototype PROTOTYPE = new Prototype(
			0x19,
			new String[] { "authorization"}, 
			false,
			REQUEST, 
			ALWAYS_RENEWED) {

		@Override
		public HTTP2_Header parse(String value) {
			return new Authorization(value);
		}

		@Override
		public HTTP2_Header create() {
			return new Authorization();
		}
		
		@Override
		public HTTP2_Header retrieve(HTTP2_Message message) {
			return message.authorization;
		}
	};

	

	public String value;

	
	public Authorization() {
		super();
	}
	
	public Authorization(String value) {
		super();
		this.value = value;
	}

	@Override
	public void bind(HTTP2_Message message) {
		message.authorization = this;
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
